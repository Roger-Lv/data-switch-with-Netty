package org.bdware.sw.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.bdware.sw.dispatcher.Dispatcher;
import org.bdware.sw.handler.NettyDoipPooledClientHandler;
import org.bdware.sw.handler.SimpleChannelPooledHandler;
import org.bdware.sw.monitor.MetricsForGrafana;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NettyClientPooledChannel {
    static Logger logger = LogManager.getLogger(NettyClientPooledChannel.class);
    protected SimpleChannelPooledHandler handler;
    public FixedChannelPool fixedChannelPool;
    protected int timeoutSecond = 5;
    //pool
    int MAX_CONNECTIONS;
    private ChannelHealthChecker healthCheck = ChannelHealthChecker.ACTIVE;
    //超时新建立连接策略 当检测到获取连接超时时，此时新建一个连接
    FixedChannelPool.AcquireTimeoutAction acquireTimeoutAction = FixedChannelPool.AcquireTimeoutAction.NEW;
    int acquireTimeoutMillis = 4000;
    int maxPendingAcquires = 100000;
    int maxNotWritableRetry = 5;
    boolean releaseHealthCheck = true;
    Bootstrap b;
    static EventLoopGroup group;
    boolean splitEnvelop;
    int maxFrameLength;
    ClientConfig config;
    String host;
    int port;
    MetricsForGrafana metrics;

    //    ExecutorService executorService;
    public NettyClientPooledChannel(ClientConfig config, int maxConnections, MetricsForGrafana metrics) throws URISyntaxException {
        this.config = config;
        this.MAX_CONNECTIONS = maxConnections;
        URIBuilder uriBuilder = new URIBuilder(config.url);
        this.host = uriBuilder.getHost();
        this.port = uriBuilder.getPort();
        this.metrics = metrics;
//        executorService =
//                new ThreadPoolExecutor(
//                        8, // corePoolSize (number of threads to keep in the pool)
//                        16, // maximumPoolSize (maximum number of threads allowed in the pool)
//                        0L, TimeUnit.MILLISECONDS, // keepAliveTime (idle threads will be terminated after this duration)
//                        // new PriorityBlockingQueue<>(), // workQueue (a queue to hold tasks until they are executed)
//                        new LinkedBlockingQueue<>(),
//                        r -> {
//                            Thread t = new Thread(r);
//                            t.setDaemon(true);
//                            return t;
//                        }
//                );
        init(config);
    }

    public void init(ClientConfig config) {
        this.splitEnvelop = false;
        this.maxFrameLength = 5 * 1024 * 1024;
        synchronized (NettyClientPooledChannel.class) {
            if (group == null)
                group = new NioEventLoopGroup(new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = Executors.defaultThreadFactory().newThread(r);
                        thread.setDaemon(true);
                        return thread;
                    }
                });
        }
        b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_LINGER, 0)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                        new WriteBufferWaterMark(2 * maxFrameLength, 10 * maxFrameLength))
                .option(ChannelOption.TCP_NODELAY, true);
        handler = new SimpleChannelPooledHandler(metrics);

        fixedChannelPool = new FixedChannelPool(b.remoteAddress(new InetSocketAddress(host, port)), handler, healthCheck, acquireTimeoutAction, acquireTimeoutMillis,
                MAX_CONNECTIONS, maxPendingAcquires, releaseHealthCheck);
    }

    public void sendMessage(DoipMessage message, DoipMessageCallback cb) throws Exception {
        //从ChannelPool中获取一个Channel
        Channel ch = null;
        try {
            ch = acquire();
        } catch (Exception e) {
            // 这里的超时说明资源有限，不能在规定的时间内获取到ch
            logger.info("The channel is unacquirable");
            e.printStackTrace();
            throw e;
        }
        // 走到这里是能确定ch不是空的
        if (!ch.isWritable()) {
            // 这里没法将其变为可以writable的，因为解释是，如果返回的是false，则说明缓冲区已经被填满，无法再写入更多数据，如果再写入可能OOM
            // 这里参考的是seata中的写法
            try {
                channelWritableCheck(ch, message);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        //走到这里说明其实channel没啥问题
        try {
            //获取信道对应的handler对象
            final NettyDoipPooledClientHandler nettyDoipPooledClientHandler = handler.ioHandlerMap.get(ch.id());
            logger.debug("ch.id: " + ch.id());
            if (nettyDoipPooledClientHandler != null) {
                nettyDoipPooledClientHandler.sendMessage(message, ch, cb);
                logger.debug("channel send message");
            }
        } catch (Exception e) {
            if (Dispatcher.switchNodeManager.cacheForAddress.containsKey(message.header.parameters.id))
                //如果连接不上，删除定位的缓存（有问题），此时下一条请求会变为
                Dispatcher.switchNodeManager.deleteCache(message.header.parameters.id);
        } finally {
            // 任何情况下都会归还ch
            release(ch);
        }

    }

    public void close(Channel channel) {
        try {
            channel.unsafe().closeForcibly();
        } catch (Throwable e) {
        }

        if (handler != null) {
            try {
                channel.close();
            } catch (Throwable e) {
            }
        }
    }

    // 获取连接
    public Channel acquire() throws Exception {
        try {
            Future<Channel> fch = fixedChannelPool.acquire(); // 【1】
            // 这里连续n个获取连接超时后，因为没有归还连接，会造成连接池可用连接耗尽，最终导致服务不可用。注：n为连接池的数量
            // 因为需要用到AcquireTimeoutAction策略，超时后会建立新的连接，因此这里不需要超时了
            Channel ch = fch.get();
            //如果没连接，重连一下
            if (!isConnected(ch)) {
                reconnect(ch);
            }
            return ch;
        } catch (Exception e) {
            logger.error("Exception accurred when acquire channel from channel pool.", e);//【3】
            throw e;
        }
    }

    // 释放连接
    public void release(Channel channel) {
        try {
            if (channel != null) {
                fixedChannelPool.release(channel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void channelWritableCheck(Channel channel, DoipMessage msg) throws Exception {
        int tryTimes = 0;
        while (!channel.isWritable()) {
            try {
                tryTimes++;
                if (tryTimes > getMaxNotWritableRetry()) {
                    logger.error("Channel is not writable, max retry times reached. channel:{}", channel.id());
                    release(channel);
                    throw new IOException();
                }
                wait(50);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public void connect(Channel channel) throws InterruptedException {
        channel.connect(new InetSocketAddress(host, port)).sync();
    }

    public void reconnect(Channel channel) throws InterruptedException {
        connect(channel);
    }

    public boolean isConnected(Channel channel) {
        return channel != null && channel.isOpen() && channel.isActive() && channel.isWritable();
    }

    public int getMaxNotWritableRetry() {
        return maxNotWritableRetry;
    }

    public void setTimeoutSecond(int ts) {
        this.timeoutSecond = ts;
    }
}
