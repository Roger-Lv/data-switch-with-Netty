package org.bdware.sw.listener;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.codec.DatagramPacketToMessageEnvelopeCodec;
import org.bdware.doip.codec.MessageEnvelopeAggregator;
import org.bdware.doip.codec.MessageEnvelopePrinter;
import org.bdware.doip.endpoint.server.DoipListenerConfig;
import org.bdware.doip.endpoint.server.NettyUDPDoipListener;
import org.bdware.sw.IrpDetector;
import org.bdware.sw.SwitchManagementIntf;
import org.bdware.sw.dispatcher.Dispatcher;
import org.bdware.sw.dp.DPHandler;
import org.bdware.sw.handler.DOHandler;
import org.bdware.sw.handler.SwitchHandler;
import org.bdware.sw.monitor.MetricsForGrafana;
import org.bdware.sw.statistics.Statistics;

import java.util.List;

public class DataSwitchUDPListener extends NettyUDPDoipListener implements NettyDataSwitchListener {


    static Logger logger = LogManager.getLogger(DataSwitchUDPListener.class);
    private final String host;
    private final String switchID;
    private Channel ch;
    private int port;
    public final Dispatcher dispatcher;
    private Runnable afterStartCallback;
    public Statistics statistics;
    public List<IrpDetector.StaticRouteEntry> entryList;
    public SwitchManagementIntf switchManagementIntf;
    public MetricsForGrafana metrics;

    public DataSwitchUDPListener(String host, int port, SwitchManagementIntf switchManagementIntf, String switchID, DoipListenerConfig listenerConfig, EndpointConfig config, List<IrpDetector.StaticRouteEntry> entryList, Runnable afterStartCallback, MetricsForGrafana metrics) throws Exception {
        super(port, listenerConfig);
        this.port = port;
        this.dispatcher = new Dispatcher(host, port, config);
        this.host = host;
        this.switchID = switchID;
        this.afterStartCallback = afterStartCallback;
        this.statistics = new Statistics();
        this.entryList = entryList;
        this.switchManagementIntf = switchManagementIntf;
        this.metrics = metrics;
    }


    @Override
    public void start() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .localAddress(port)
                    .option(ChannelOption.SO_BROADCAST, true);
            b.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                    new WriteBufferWaterMark(0, 100));
            SwitchHandler switchHandler = new SwitchHandler(dispatcher, switchManagementIntf, host + ":" + port, metrics);
            DOHandler doHandler = new DOHandler(dispatcher, statistics, switchHandler, entryList, metrics);
            b.handler(
                    new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            logger.info("establish a channel:" + port);

                            ch.pipeline()
                                    .addLast(new DatagramPacketToMessageEnvelopeCodec())
                                    .addLast(new MessageEnvelopePrinter())
                                    .addLast(new MessageEnvelopeAggregator(MessageEnvelopeAggregator.MTU_802_3 - 24));
                            listenerConfig.addExtraCodec(ch.pipeline());
                            ch.pipeline().addLast(new DPHandler())
                                    .addLast(switchHandler)
                                    .addLast(doHandler);

                        }
                    });

            ch = b.bind().syncUninterruptibly().channel();
            logger.info("Data-Switch UDP listener start at:" + port);
            if (startServerCallback != null)
                startServerCallback.onSuccess(port);
            afterStartCallback.run();
            ch.closeFuture().sync();
        } catch (Exception e) {
            if (startServerCallback != null)
                startServerCallback.onException(e);
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
