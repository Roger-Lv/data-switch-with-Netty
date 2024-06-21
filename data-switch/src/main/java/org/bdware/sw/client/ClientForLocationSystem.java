package org.bdware.sw.client;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.cluster.client.DOAConfigBuilder;
import org.bdware.doip.cluster.client.DoaClusterClient;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.sw.SM3Tool;
import org.bdware.sw.SWConfig;

public class ClientForLocationSystem extends DoaClusterClient {
    static Logger LOGGER = LogManager.getLogger(ClientForLocationSystem.class);

    public ClientForLocationSystem(String irpConfig) {
        super(DOAConfigBuilder.withIrpConfig(irpConfig));
        updateEntranceLater();
    }

    public void updateEntranceLater() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String switchID = SWConfig.globalConfig.id;
                    String switchDOIPURL = "tcp://" + SWConfig.globalConfig.ip + ":" + SWConfig.globalConfig.port;
                    updateEntrance(SM3Tool.toSM3(switchID), switchDOIPURL);
                    updateEntrance(SM3Tool.toSM3(SWConfig.globalConfig.gatewayId), SWConfig.globalConfig.gatewayDOIPURL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static boolean disableUpdate = true;

    public DoipMessage updateData(String sm3Key, String id) {
        if (disableUpdate) return null;
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("whatever", BasicOperations.Update.getName());
        builder.addAttributes("dol", sm3Key);
        builder.addAttributes("entranceDol", SM3Tool.toSM3(id));
        builder.addAttributes("type", "Data");
        builder.setBody("0".getBytes());
        return sendMessageSync(builder.create(), 5000, false);
    }

    public DoipMessage updateEntrance(String sm3Key, String address) {
        if (disableUpdate) return null;
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("whatever", BasicOperations.Update.getName());
        builder.addAttributes("dol", sm3Key);
        builder.addAttributes("type", "Entrance");
        builder.addAttributes("address", address);
        builder.setBody("0".getBytes());
        LOGGER.info("Update Entrance:" + sm3Key + "-->" + address);
        return sendMessageSync(builder.create(), 5000, false);
    }

    public JsonObject getData(String sm3Key) {
        try {
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createRequest("whatever", BasicOperations.Retrieve.getName());
            builder.addAttributes("dol", sm3Key);
            builder.setBody("0".getBytes());
            DoipMessage response = sendMessageSync(builder.create(), 5000, false);
            JsonObject jo = new JsonObject();
            jo.addProperty("result", response.header.parameters.attributes.get("address").getAsString().replaceAll("tcp://", ""));
            return jo;
        } catch (Exception e) {
            StackTraceElement[] stackTraceElements = e.getStackTrace();
            for (int i = 0; i < 1 && i < stackTraceElements.length; i++)
                LOGGER.error(e.getMessage() + "->" + stackTraceElements[i].toString());
        }
        return new JsonObject();

    }
}


