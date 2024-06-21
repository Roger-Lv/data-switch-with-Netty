package org.bdware.sw.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.sw.IrpDetector;
import org.bdware.sw.SWConfig;
import org.bdware.sw.client.ClientForDigitalSpace;
import org.bdware.sw.client.clusterclient.ClusterClientForSwitch;
import org.bdware.sw.dispatcher.DispatchAnswer;
import org.bdware.sw.dispatcher.Dispatcher;
import org.bdware.sw.monitor.MetricsForGrafana;
import org.bdware.sw.statistics.Statistics;
import org.zz.gmhelper.SM2KeyPair;
import org.zz.gmhelper.SM2Util;

import java.util.List;

/**
 * @Description DataSwitcher Listener的handler
 **/
@ChannelHandler.Sharable
public class DOHandler extends SimpleChannelInboundHandler<DoipMessage> {
    static Logger logger = LogManager.getLogger(DOHandler.class);
    private final IrpDetector irpDetector;
    // 三个clusterclient,一个应用于数字空间，一个应用于定位系统，一个应用于switch
    //client for digitalSpaceClusterClient
    public ClientForDigitalSpace digitalSpaceClusterClient;
    //client for location system&&Publish to MockKV
    SM2KeyPair pair = SM2Util.generateSM2KeyPair();
    //client for switch
    public ClusterClientForSwitch clientForSwitch;
    //    public ClientForSwitch clientForSwitch = new ClientForSwitch();
//    public ClientForSwitchWithoutPool clientForSwitch = new ClientForSwitchWithoutPool();
    // Dispatcher: 解析DOIP请求，并根据头部信息将任务分发给LocationSystem/Switch/DigitalSpace（三种Client）
    public Dispatcher dispatcher;
    // RetrieveFuntions
    public SwitchFunctions switchFunctions;
    //Statistics
    public Statistics statistics;
    //metricsForGrafana
    private MetricsForGrafana metrics;
    private final SwitchHandler switchHandler;

    public DOHandler(Dispatcher dispatcher, Statistics statistics, SwitchHandler switchHandler,
                     List<IrpDetector.StaticRouteEntry> entryList, MetricsForGrafana metrics) {
        this.dispatcher = dispatcher;
//        logger.info("endpointConfig:" + dispatcher.endpointConfig);
        this.statistics = statistics;
        this.irpDetector = new IrpDetector(entryList);
        this.metrics = metrics;
        this.switchFunctions = new SwitchFunctions(metrics);
        clientForSwitch = new ClusterClientForSwitch(100, metrics);
        this.switchHandler = switchHandler;
        switchHandler.setDOHandler(this);
        resetClientForDigitalSpace();
    }

    public void resetClientForDigitalSpace() {
        digitalSpaceClusterClient = new ClientForDigitalSpace(SWConfig.globalConfig.gatewayIRPURL);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DoipMessage msg) {
        if (msg.header.parameters == null || msg.header.parameters.operation == null) {
            logger.error("Invalid message, header: ");
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createRequest("ToClient", BasicOperations.Unknown.getName());
            builder.setBody("wrong doipmessage".getBytes());
            ctx.writeAndFlush(builder.create());
            return;
        }
//        ctx.writeAndFlush(msg);
//        synchronized (statistics){
//                            statistics.doipOp++;
//                        }
        if (dispatcher.isRequest(msg)) {
            try {
                DispatchAnswer result = dispatcher.dispatch(msg);
                int situation = result.situation;
                switch (situation) {
                    //将请求移交给ClusterClientForDigitalSpace
                    case 0:
                        logger.info("0.将请求移交给ClusterClientForDigitalSpace:" + msg.header.parameters.id);
                        switchFunctions.retrieveByClusterClientForDigitalSpace(ctx, msg, result, digitalSpaceClusterClient);
                        synchronized (statistics) {
                            statistics.doipOp++;
                        }
                        // metrics inc
                        metrics.doipData(msg.header.parameters.operation).inc();
                        break;
                    //将请求移交给ClientForSwitch
                    case 1:
                        logger.info("1.将请求移交给ClientForSwitch:" + msg.header.parameters.id);
                        switchFunctions.retrieveByClientForSwitch(ctx, msg, result, clientForSwitch, dispatcher);
                        synchronized (statistics) {
                            statistics.doipOp++;
                        }
                        // metrics inc
                        metrics.doipData(msg.header.parameters.operation).inc();
                        break;
                    //get mockKV
                    case 2:
                        logger.info("2.get from router network:" + msg.header.parameters.id);
                        switchFunctions.retrieveByGetKV(ctx, msg, irpDetector, switchHandler.locationSystemClusterClient, clientForSwitch, dispatcher);
                        synchronized (statistics) {
                            statistics.doipOp++;
                        }
                        // metrics inc
                        metrics.doipData(msg.header.parameters.operation).inc();
                        break;
                    default:
                        metrics.doipWrongDataCounter(msg.header.parameters.operation).inc();
                        logger.error("Unknown wrong situation");
                }
            } catch (Exception e) {
                logger.error("Exception: " + e.getMessage());
                switchFunctions.handleWrongMsg(msg, ctx);
                metrics.doipWrongDataCounter(msg.header.parameters.operation).inc();
                e.printStackTrace();
            }


        } else {
            logger.error("not doip request");
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //出现异常的时候执行的动作（打印并关闭通道）
        cause.printStackTrace();
        ctx.channel().close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }


}
