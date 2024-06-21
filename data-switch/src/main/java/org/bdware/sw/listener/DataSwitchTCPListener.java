package org.bdware.sw.listener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.codec.MessageEnvelopeAggregator;
import org.bdware.doip.codec.MessageEnvelopeCodec;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;
import org.bdware.doip.endpoint.server.DoipListenerConfig;
import org.bdware.doip.endpoint.server.NettyTCPDoipListener;
import org.bdware.sw.IrpDetector;
import org.bdware.sw.SwitchManagementIntf;
import org.bdware.sw.dispatcher.Dispatcher;
import org.bdware.sw.handler.DOHandler;
import org.bdware.sw.handler.NetworkTrafficStatHandler;
import org.bdware.sw.handler.SwitchHandler;
import org.bdware.sw.monitor.MetricsForGrafana;
import org.bdware.sw.statistics.Statistics;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.List;

public class DataSwitchTCPListener extends NettyTCPDoipListener implements NettyDataSwitchListener {


    static Logger logger = LogManager.getLogger(DataSwitchTCPListener.class);
    private final String host;
    private Channel ch;
    private int port;
    public final Dispatcher dispatcher;
    private Runnable afterStartCallback;
    public Statistics statistics;
    public SwitchHandler switchHandler;
    public DOHandler switchDOHandler;
    public MetricsForGrafana metrics;
    public NetworkTrafficStatHandler networkTrafficStatHandler;
    List<IrpDetector.StaticRouteEntry> entryList;

    public DataSwitchTCPListener(String host, int port, SwitchManagementIntf switchManagementIntf, DoipListenerConfig listenerConfig, EndpointConfig config, List<IrpDetector.StaticRouteEntry> entryList, Runnable afterStartCallback) throws URISyntaxException, InterruptedException {
        super(port, listenerConfig);
        this.port = port;
        this.dispatcher = new Dispatcher(host, port, config);
        this.host = host;
        this.afterStartCallback = afterStartCallback;
        this.statistics = new Statistics();
        this.metrics = new MetricsForGrafana();
        this.entryList = entryList;
        this.switchHandler = new SwitchHandler(dispatcher, switchManagementIntf, host + ":" + port, this.metrics);
        this.networkTrafficStatHandler = new NetworkTrafficStatHandler(this.metrics);
        //DOHandler的创建留到添加handler的时候
    }

    public Statistics getStatistics() {
        return this.statistics;
    }


    @Override
    public void start() {
        int maxFrame = 5 * 1024 * 1024;
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(this.port));
            DOHandler doHandler = new DOHandler(dispatcher, statistics, switchHandler, entryList, metrics);
            //childHandler 只对workerGroup起作用，监听已经连接的客户端的Channel的动作和状态
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    pipeline.addLast(new LengthFieldBasedFrameDecoder(maxFrame, 20, 4, 0, 0))
                            //贴到这后面
                            .addLast(networkTrafficStatHandler).addLast(new MessageEnvelopeCodec()).addLast(new MessageEnvelopeAggregator(maxFrame - MessageEnvelope.ENVELOPE_LENGTH));
                    listenerConfig.addExtraCodec(pipeline);
                    pipeline.addLast(switchHandler).addLast(doHandler);
//                                    .addLast(new DPHandler())
                    //这里注释掉的原因是加上跑不通，就是发送请求会失败
                }
            });

            ch = b.bind().syncUninterruptibly().channel();
            logger.info("在" + ch.localAddress() + "上开启监听");
            afterStartCallback.run();
            ch.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bossGroup.shutdownGracefully().sync();
                workerGroup.shutdownGracefully();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
