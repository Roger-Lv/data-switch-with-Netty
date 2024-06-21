package org.bdware.sw.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.audit.AuditIrpMessageFactory;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.audit.EndpointInfo;
import org.bdware.doip.audit.client.AuditIrpClientChannel;
import org.bdware.doip.audit.client.DoIdWrapper;
import org.bdware.doip.audit.writer.AuditConfig;
import org.bdware.doip.audit.writer.AuditRepo;
import org.bdware.doip.audit.writer.ConfigurableAuditConfig;
import org.bdware.doip.encrypt.SM2Signer;
import org.bdware.irp.client.IrpClient;
import org.bdware.irp.exception.IrpClientException;
import org.bdware.irp.irpclient.IrpClientChannel;
import org.bdware.irp.irpclient.IrpMessageCallback;
import org.bdware.irp.irplib.core.*;
import org.bdware.irp.irplib.exception.IrpConnectException;
import org.bdware.irp.irplib.exception.IrpMessageDecodeException;
import org.bdware.irp.stateinfo.StateInfoBase;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.zz.gmhelper.SM2KeyPair;
import org.zz.gmhelper.SM2Util;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FixedAuditIrpClient implements IrpClient {
    static Logger LOGGER = LogManager.getLogger(FixedAuditIrpClient.class);
    static Map<String, IrpClientChannel> cachedChannels = new ConcurrentHashMap<>();
    IrpClientChannel cachedChannel;
    String cachedUrl;
    IrpClientChannel irpChannel;
    String serverURL = null;
    ResponseCallback rcb;
    ResponseContainer container;
    EndpointInfo resInfo;
    IrpRequestFactory factory = new IrpRequestFactory(null);
    SM2Signer signer;
    ConfigurableAuditConfig auditConfig;
    DoIdWrapper clientDoid;
    DoIdWrapper serverDoId;

    public FixedAuditIrpClient(EndpointConfig config) {
        this(null, config);
    }

    public FixedAuditIrpClient(String clientDoid, EndpointConfig config) {

        this.serverURL = this.cachedUrl = config.routerURI;
        if (config.publicKey != null && config.privateKey != null) {
            signer = new SM2Signer(SM2KeyPair.fromJson(new Gson().toJson(config)));
        } else
            signer = new SM2Signer(SM2Util.generateSM2KeyPair());

        this.clientDoid = new DoIdWrapper(clientDoid);
        this.serverDoId = new DoIdWrapper(null);
        container = new ResponseContainer();
        rcb = new ResponseCallback(container);
        auditConfig = AuditConfig.newConfigurableInstance(new AuditRepo(clientDoid, null), config.auditType, config.extraConfig);
        if (config.repoName != null && config.publicKey != null && config.privateKey != null) {
            String toSign = config.repoName + "|" + config.publicKey;
            SM2KeyPair pair = SM2KeyPair.fromJson(new Gson().toJson(config));

            try {
                String sign = ByteUtils.toHexString(SM2Util.sign(pair.getPrivateKeyParameter(), toSign.getBytes(StandardCharsets.UTF_8)));

                authInfoFromUpperRouter(config.publicKey, config.repoName, sign);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        asyncSetAuditInfo(config);
        irpChannel = getOrCreateChannel(serverURL);
    }

    private void asyncSetAuditInfo(EndpointConfig config) {
        AuditRepo auditInfo = queryAuditRepo();
        try {
            if (auditInfo != null) {
                auditConfig.setAuditRepo(auditInfo);
                serverDoId.setDoId(auditInfo.auditDoid.replaceAll("/.*$", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            auditConfig.changeAuditConfig(AuditConfig.newInstance(null, config.auditType, config.extraConfig));
        }
    }


    @Override
    public String reRegister(StateInfoBase hr) throws IrpClientException {
        IrpMessage req = factory.newIrsUpdateDoidRequest(hr.identifier, hr.getHandleValues());
        setServerAddress(serverURL);
        sendMessage(req, rcb);
        IrpResponse res = waitForResponse();
        return res.result;
    }

    @Override
    public String unRegister(String doid) {
        IrpMessage req = factory.newIrsDeleteDoidRequest(doid);
        setServerAddress(serverURL);
        sendMessage(req, rcb);
        IrpResponse res = waitForResponse();
        return res.result;
    }


    @Override
    public List<String> batchRegister(StateInfoBase hr, int count) {
        IrpMessage req = factory.newIrsBatchCreateDoidRequest(hr.getHandleValues(), count);
        setServerAddress(serverURL);

        sendMessage(req, rcb);
        IrpResponse res = waitForResponse();
        return res.getDoidList();
    }

    private String authInfoFromUpperRouter(String pubkey, String name, String signInfo) throws IrpClientException {
        LOGGER.debug(String.format("n&p:%s|%s sign:%s", name, pubkey, signInfo));
        if (pubkey == null || name == null || signInfo == null) {
            throw new IrpClientException("authinfo is null!");
        }
        IrpMessage req = IrpForRouterRequest.newVerifyRouterAuthRequest(pubkey, name, signInfo);
        sendMessage(req, rcb);
        IrpResponse res = waitForResponse();
        if (res.header.responseCode != IrpMessageCode.RC_SUCCESS || !(res instanceof IrpForRouterResponse)) {
            LOGGER.error("Verify the router from upper router failed: " + res.getResponseMessage());

            throw new IrpClientException("Verify the router from upper router failed!");
        }
        IrpForRouterResponse response = (IrpForRouterResponse) res;
        resInfo = EndpointInfo.fromJson(response.routerInfo);
        return new Gson().toJson(resInfo);
    }

    public synchronized StateInfoBase resolveDirectly(String doid) throws IrpClientException {
        if (doid == null) {
            throw new IrpClientException("doid is null!");
        }
        String prefix = doid;
        //resolve
        String nextServerUrl = this.serverURL;
        setServerAddress(nextServerUrl);
        try {
            reconnect();
        } catch (IrpConnectException e) {
            e.printStackTrace();
        }
        IrpMessage req = factory.newIrsResolveRequest(prefix, null);

        irpChannel.sendMessage(req, rcb);
        IrpResponse res = waitForResponse();
        String doidInfo;
        if (res.header.responseCode == IrpMessageCode.RC_SUCCESS) {
            //get the right one
            StateInfoBase base = new StateInfoBase();
            base.identifier = doid;
            base.handleValues = res.getDoidValues();
            return base;
        } else if (res.header.responseCode == IrpMessageCode.RC_NA_DELEGATE) {
            StateInfoBase base = new StateInfoBase();
            base.identifier = doid;
            base.handleValues = JsonParser.parseString("{\"msg\":\"need delegate\"}").getAsJsonObject();
            return base;
        } else if (res.header.responseCode == IrpMessageCode.RC_ERROR) {
            StateInfoBase base = new StateInfoBase();
            base.identifier = doid;
            base.handleValues = JsonParser.parseString("{\"msg\":\"error\"}").getAsJsonObject();
            return base;
        } else {
            throw new IrpClientException("Unhandled response code:"
                    + res.header.responseCode);
        }
    }

    @Override
    public synchronized StateInfoBase resolve(String doid) throws IrpClientException {
        return resolve(doid, true);
    }

    @Override
    public String register(StateInfoBase jo) throws IrpClientException {
        IrpMessage req = jo.identifier == null ?
                factory.newIrsCreateDoidRequest(jo.getHandleValues())
                : factory.newIrsCreateDoidRequest(jo.identifier, jo.getHandleValues());
        setServerAddress(serverURL);
        sendMessage(req, rcb);
        IrpResponse res = waitForResponse();
        if (res.header.responseCode == IrpMessageCode.RC_NA_DELEGATE) {
            //get the next router
            String nextServerUrl;
            if (res.delegateTargetURL != null) {
                if (!res.delegateTargetURL.startsWith("tcp://"))
                    nextServerUrl = "tcp://" + res.delegateTargetURL;
                else nextServerUrl = res.delegateTargetURL;
            } else {
                throw new IrpClientException("The next router address is wrong:"
                        + res.header.responseCode);
            }
            setServerAddress(nextServerUrl);
            sendMessage(req, rcb);
            res = waitForResponse();
        }
        return res.getDoid();
    }


    public synchronized StateInfoBase resolve(String doid, boolean recursive) throws IrpClientException {
        if (doid == null) {
            throw new IrpClientException("doid is null!");
        }
        String prefix = doid;
        //resolve
        boolean stopFlag = false;
        String nextServerUrl = this.cachedUrl;
        String doidInfo = null;
        //query the next router until find the right one
        while (!stopFlag) {
            IrpResponse res = null;
            try {
                setServerAddress(nextServerUrl);
                reconnect();
                IrpMessage req = factory.newIrsResolveRequest(prefix, null);
                req.header.setRecursiveFlag(recursive);
                sendMessage(req, rcb);
                res = waitForResponse();
//                LOGGER.debug(res.toString());
            } catch (Exception e) {

                e.printStackTrace();
                if (e instanceof IrpClientException)
                    throw (IrpClientException) e;
            }
            if (res != null && res.header.responseCode == IrpMessageCode.RC_SUCCESS) {
                //get the right one
                StateInfoBase base = new StateInfoBase();
                base.identifier = doid;
                base.handleValues = res.getDoidValues();
                if (base.handleValues.has("type") && base.handleValues.get("type").equals("HS_ALIAS")) {
                    if (base.handleValues.has("nextServerUrl"))
                        nextServerUrl = base.handleValues.get("nextServerUrl").getAsString();
                    if (!nextServerUrl.startsWith("tcp://"))
                        nextServerUrl = "tcp://" + nextServerUrl;
                    prefix = res.getDoid();
                    stopFlag = false;
                } else
                    return base;
            } else if (res.header.responseCode == IrpMessageCode.RC_NA_DELEGATE) {
                //get the next router
                if (res.delegateTargetURL != null) {
                    if (!res.delegateTargetURL.startsWith("tcp://"))
                        nextServerUrl = "tcp://" + res.delegateTargetURL;
                    else nextServerUrl = res.delegateTargetURL;
                    stopFlag = false;
                } else {
                    throw new IrpClientException("The next router address is wrong:"
                            + res.header.responseCode);
                }
            } else if (res.header.responseCode == IrpMessageCode.RC_ERROR) {
                //LOGGER.info(EncoderUtils.decodeString(res.responseMessage));
                stopFlag = true;
            } else {
                throw new IrpClientException("Unhandled response code:"
                        + res.header.responseCode);
            }
        }
        return null;
    }

    public AuditRepo queryAuditRepo() {
        try {
            IrpMessage req = AuditIrpMessageFactory.newAuditRepoRequest();
            setServerAddress(serverURL);
            sendMessage(req, rcb);
            IrpResponse res = waitForResponse();
            return AuditIrpMessageFactory.getAuditRepo(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JsonObject queryIdentifierByOffsetAndCount(int offset, int count, boolean queryByCreateTime) throws IrpClientException, IrpMessageDecodeException {
        IrpMessage req = factory.newQueryDOByOffsetRequest(offset, count, queryByCreateTime);
        setServerAddress(serverURL);
        sendMessage(req, rcb);
        IrpResponse res = waitForResponse();
        if (res.header.responseCode == IrpMessageCode.RC_SUCCESS) {
            return IrpMessage.fromByte2Json(res.doidValues);
        }
        JsonObject jo = new JsonObject();
        jo.addProperty("status", "error");
        jo.addProperty("msg", res.getResponseMessage());
        return jo;
    }

    //the router info is in the array[0], compatible with irs code/encode
    IrpResponse waitForResponse() {
        container.response = null;
        synchronized (rcb) {
            try {
                rcb.wait(5000L);
            } catch (InterruptedException e) {
                //    e.printStackTrace();
            }
        }
        if (container.response == null) {
            container.response = IrpResponse.newErrorResponse(
                    IrpMessageCode.OC_RESERVED,
                    IrpMessageCode.RC_ERROR,
                    "Server response timeout!");
        }
        return container.response;
    }

    public void close() {
        if (irpChannel != null) {
            irpChannel.close();
            irpChannel = null;
        }
    }


    private synchronized IrpClientChannel getOrCreateChannel(String serverURL) {
        IrpClientChannel ret;
        synchronized (cachedChannels) {
            if (cachedChannels.containsKey(signer.getKeyPair().getPublicKeyStr() + "|" + serverURL)) {
                ret = cachedChannels.get(signer.getKeyPair().getPublicKeyStr() + "|" + serverURL);
            } else {
                ret = new AuditIrpClientChannel(clientDoid, serverDoId, signer, auditConfig);
                cachedChannels.put(signer.getKeyPair().getPublicKeyStr() + "|" + serverURL, ret);
            }
        }
        return ret;
    }

    public void close(String serverURL) {
        synchronized (cachedChannels) {
            IrpClientChannel channel = cachedChannels.get(signer.getKeyPair().getPublicKeyStr() + "|" + serverURL);
            channel.close();
            cachedChannels.remove(serverURL);
        }
    }

    public static void closeAll() {
        synchronized (cachedChannels) {
            for (IrpClientChannel channel : cachedChannels.values()) {
                channel.close();
            }
            cachedChannels.clear();
        }
    }

    public void setServerAddress(String newURL) {
        this.cachedUrl = newURL;
    }

    @Override
    public void connect(String url) {
        cachedUrl = url;
    }

    @Override
    public void connect(String clientID, String LHSUrl, IrpMessageSigner signer) {
        cachedUrl = LHSUrl;
        this.signer = (SM2Signer) signer;
        try {
            reconnect();
        } catch (IrpConnectException e) {
            throw new RuntimeException(e);
        }
    }

    public void reconnect() throws IrpConnectException {
        if (cachedUrl == null) throw (new IrpConnectException("target URL not set, use .connect(url) first"));
        cachedChannel = getOrCreateChannel(cachedUrl);
        if (cachedChannel == null) return;
        try {
            if (!cachedChannel.isConnected()) {
                LOGGER.info("Reconnect the next router: " + cachedUrl);
                cachedChannel.connect(cachedUrl);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected() {
        return false;
    }


    public void sendMessage(IrpMessage msg, IrpMessageCallback cb) {
        // connect();
        try {
            reconnect();
        } catch (IrpConnectException e) {
            e.printStackTrace();
        }
        if (cachedChannel == null || !cachedChannel.isConnected()) {
            LOGGER.warn("channel not connect yet!");
            return;
        }
        //msg.setRecipientID(recipientID);
        cachedChannel.sendMessage(msg, cb);
    }

    public IrpResponse getLastResponse() {
        return container.response;
    }

    class ResponseCallback implements IrpMessageCallback {
        ResponseContainer responseContainer;

        public ResponseCallback(ResponseContainer container) {
            responseContainer = container;
        }

        @Override
        public synchronized void onResult(IrpMessage msg) {
            responseContainer.response = (IrpResponse) msg;
            this.notifyAll();
        }
    }

    static class ResponseContainer {
        IrpResponse response;
    }
}
