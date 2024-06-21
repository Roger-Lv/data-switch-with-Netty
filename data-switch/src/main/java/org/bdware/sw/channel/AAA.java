package org.bdware.sw.channel;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.endpoint.client.*;
import io.netty.util.concurrent.Future;
import org.bdware.sw.dispatcher.Dispatcher;
import org.bdware.sw.handler.NettyDoipPooledClientHandler;
import org.bdware.sw.handler.SimpleChannelPooledHandler;
import org.apache.http.client.utils.URIBuilder;
import java.net.*;
import java.util.concurrent.*;

public class AAA {
    static Logger logger = LogManager.getLogger(NettyClientPooledChannel.class);
    protected SimpleChannelPooledHandler handler;
    public FixedChannelPool fixedChannelPool;
    protected int timeoutSecond = 5;
    //pool
    int MAX_CONNECTIONS;
    private ChannelHealthChecker healthCheck = ChannelHealthChecker.ACTIVE;
    //超时新建立连接策略
    FixedChannelPool.AcquireTimeoutAction acquireTimeoutAction = FixedChannelPool.AcquireTimeoutAction.NEW;
    int acquireTimeoutMillis = 10000;
    int maxPendingAcquires = 100000;
    boolean releaseHealthCheck=true;
    Bootstrap b;
    static EventLoopGroup group;
    boolean splitEnvelop;
    int maxFrameLength;
    ClientConfig config;
    String host;
    int port;
    ExecutorService executorService;

    public AAA(ClientConfig config, int maxConnections) throws URISyntaxException {
        this.config = config;
        this.MAX_CONNECTIONS = maxConnections;
        URIBuilder uriBuilder = new URIBuilder(config.url);
        this.host = uriBuilder.getHost();
        this.port = uriBuilder.getPort();
        executorService =
                new ThreadPoolExecutor(
                        2, // corePoolSize (number of threads to keep in the pool)
                        4, // maximumPoolSize (maximum number of threads allowed in the pool)
                        0L, TimeUnit.MILLISECONDS, // keepAliveTime (idle threads will be terminated after this duration)
                        // new PriorityBlockingQueue<>(), // workQueue (a queue to hold tasks until they are executed)
                        new LinkedBlockingQueue<>(),
                        r -> {
                            Thread t = new Thread(r);
                            t.setDaemon(true);
                            return t;
                        }
                );
        init(config);
    }

    public void init(ClientConfig config){
        this.splitEnvelop=false;
        this.maxFrameLength=5 * 1024 * 1024;
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
        handler = new SimpleChannelPooledHandler();

        fixedChannelPool = new FixedChannelPool(b.remoteAddress(new InetSocketAddress(host,port)),handler,healthCheck,acquireTimeoutAction,acquireTimeoutMillis,
                MAX_CONNECTIONS,maxPendingAcquires,releaseHealthCheck);
    }

    public void sendMessage(DoipMessage message, DoipMessageCallback cb) throws Exception {
        //从ChannelPool中获取一个Channel
        executorService.submit(() -> {
            Channel ch = null;
            try {
                ch = acquire();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                //获取信道对应的handler对象
                final NettyDoipPooledClientHandler nettyDoipPooledClientHandler = handler.ioHandlerMap.get(ch.id());
                if(nettyDoipPooledClientHandler!=null){
                    nettyDoipPooledClientHandler.sendMessage(message,ch, cb);
                    logger.debug("channel send message");
                }
            }catch (Exception e){
                if (Dispatcher.switchNodeManager.cacheForAddress.containsKey(message.header.parameters.id))
                    Dispatcher.switchNodeManager.deleteCache(message.header.parameters.id);
            }
            finally {
                release(ch);
            }
        });

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
            Future<Channel> fch =fixedChannelPool.acquire(); // 【1】
            // 这里连续n个获取连接超时后，因为没有归还连接，会造成连接池可用连接耗尽，最终导致服务不可用。注：n为连接池的数量
            // 解决方案：需要实现AcquireTimeoutAction的NEW或FAIL策略，因为您实现的代码创建FixedChannelPool连接池时AcquireTimeoutAction参数传的是null
            // Channel ch = fch.get(timeoutMillis, TimeUnit.MILLISECONDS);
            //  【修复】因为需要用到AcquireTimeoutAction策略，因此这里不需要超时了
            Channel ch = fch.get();
            return ch;
        } catch (Exception e) {
            logger.error("Exception accurred when acquire channel from channel pool.", e);//【3】
            throw e; //【4】
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


    public void setTimeoutSecond(int ts){
        this.timeoutSecond = ts;
    }
}
