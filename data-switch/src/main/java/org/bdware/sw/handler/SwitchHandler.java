package org.bdware.sw.handler;

import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.doipMessage.DoipResponseCode;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.encrypt.SM2Signer;
import org.bdware.sw.DataSwitch;
import org.bdware.sw.SM3Tool;
import org.bdware.sw.SWConfig;
import org.bdware.sw.SwitchManagementIntf;
import org.bdware.sw.client.ClientForLocationSystem;
import org.bdware.sw.dispatcher.Dispatcher;
import org.bdware.sw.monitor.MetricsForGrafana;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.zz.gmhelper.SM2KeyPair;
import org.zz.gmhelper.SM2Util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description DataSwitcher Listener的handler
 **/
@ChannelHandler.Sharable
public class SwitchHandler extends MessageToMessageCodec<DoipMessage, DoipMessage> {
    static Logger logger = LogManager.getLogger(SwitchHandler.class);
    private final String switchIpPort;
    //client for location system&&Publish to MockKV
    SM2KeyPair pair = SM2Util.generateSM2KeyPair();
    public ClientForLocationSystem locationSystemClusterClient;
    public Dispatcher dispatcher;
    private MetricsForGrafana metrics;
    public SwitchManagementIntf switchManagementIntf;
    private DOHandler doHandler;

    //client for switch
    public SwitchHandler(Dispatcher dispatcher, SwitchManagementIntf switchManagementIntf, String switchIpPort, MetricsForGrafana metrics) {
        this.dispatcher = dispatcher;
        this.switchIpPort = switchIpPort;
        this.switchManagementIntf = switchManagementIntf;
        locationSystemClusterClient = new ClientForLocationSystem(SWConfig.globalConfig.routerIRPURL);
        this.metrics = metrics;
    }


    protected DoipMessage processAutoRegisterDOL(DoipMessage msg) throws Exception {
        logger.info("[RegisterDOLHandler] hook success!");
        String bodyStr = msg.body.getDataAsJsonString();
        List<String> keys = extractTextFromDPML(bodyStr);
        for (String key : keys) {
            //sm3
            String sm3Key = SM3Tool.toSM3(key);
            String sm3SwitchId = SWConfig.globalConfig.id;
            DoipMessage response = locationSystemClusterClient.updateData(sm3Key, sm3SwitchId);
            //TODO the value of digitalSpaceNodeManager is unused, just make ifInCache=true
            // dispatcher.digitalSpaceNodeManager.addCache(key, new EndpointConfig()); 暂时废除，因为在digitalSpaceNodeManager中有增加cache的逻辑
        }
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("ToClient", BasicOperations.Publish.getName());
        builder.setBody("has published to LocationSystem".getBytes());
        return builder.create();
    }

    protected DoipMessage processMangementUpdate(DoipMessage msg) throws Exception {
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createResponse(DoipResponseCode.Success, msg);
        String result = "update config: " + switchManagementIntf.updateConfig(msg.header.parameters.attributes);
        if (result.contains("routerIRPURL")) {
            locationSystemClusterClient = new ClientForLocationSystem(SWConfig.globalConfig.routerIRPURL);
        }
        if (result.contains("gatewayId") || result.contains("gatewayDOIPURL")) {
            locationSystemClusterClient.updateEntranceLater();
        }
        if (result.contains("gatewayIRPURL")) {
            if (doHandler != null)
                doHandler.resetClientForDigitalSpace();
        }
        builder.setBody(result.getBytes());
        return builder.create();
    }

    private DoipMessage processManagementRetrieve(DoipMessage msg) {
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createResponse(DoipResponseCode.Success, msg);
        JsonObject jo = SWConfig.globalConfig.listConfig();
        for (String key : jo.keySet())
            builder.addAttributes(key, jo.get(key));
        return builder.create();
    }

    private List<String> extractTextFromDPML(String xmlString) {
        SAXReader reader = new SAXReader();
        List<String> ret = new ArrayList<>();
        try {
            Document document = reader.read(new StringReader(xmlString));
            List<Node> nodes = document.selectNodes("//input | //output | //algorithm");
            for (Node node : nodes) {
                String inputText = node.getText();
                ret.add(inputText);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //出现异常的时候执行的动作（打印并关闭通道）
        cause.printStackTrace();
        ctx.channel().close();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DoipMessage msg, List<Object> out) throws Exception {
        //just ignore
        out.add(msg);
    }

    SM2Signer signer = new SM2Signer(SM2Util.generateSM2KeyPair());
    static Logger LOGGER = LogManager.getLogger(SwitchHandler.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, DoipMessage msg, List<Object> out) throws Exception {
        if (msg.isRequest() && msg.header.isCommand()) {
            if (msg.header.parameters.id.equals(DataSwitch.switchId)) {  // 匹配到本交换机id
                LOGGER.info("catch switch handler msg!");
                try {
                    BasicOperations op = BasicOperations.getDoOp(msg.header.parameters.operation);
                    DoipMessage response = null;
                    switch (op) {
                        case Publish:
                            response = processAutoRegisterDOL(msg);
                            break;
                        case Update:
                            if (isManager(msg)) {
                                response = processMangementUpdate(msg);
                            } else {
                                DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
                                builder.createResponse(DoipResponseCode.Declined, msg);
                                builder.setBody(("Permission denied!").getBytes());
                                response = builder.create();
                            }
                            break;
                        case Retrieve:
                            if (isManager(msg)) {
                                LOGGER.info("verified management doip request success!");
                                response = processManagementRetrieve(msg);
                            } else {
                                DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
                                builder.createResponse(DoipResponseCode.Declined, msg);
                                builder.setBody(("Permission denied!").getBytes());
                                response = builder.create();
                            }
                            break;
                        default:
                            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
                            builder.createResponse(DoipResponseCode.Declined, msg);
                            builder.setBody(("Unsupported Operation:" + msg.header.parameters.operation).getBytes());
                            response = builder.create();
                    }
                    ctx.writeAndFlush(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    e.printStackTrace(new PrintStream(bo));
                    DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
                    builder.createResponse(DoipResponseCode.UnKnownError, msg);
                    builder.setBody(bo.toByteArray());
                    ctx.writeAndFlush(builder.create());
                }
            } else {
                LOGGER.info("not my id:" + msg.header.parameters.id + " --> myId:" + DataSwitch.switchId);
                //ignore
                out.add(msg);
            }
        } else
            //ignore
            out.add(msg);
    }


    private boolean isManager(DoipMessage msg) {
        return (signer.verifyMessage(msg) && msg.credential.getSigner().equals(SWConfig.globalConfig.managerPublicKey));
    }

    public void setDOHandler(DOHandler doHandler) {
        this.doHandler = doHandler;
    }
}
