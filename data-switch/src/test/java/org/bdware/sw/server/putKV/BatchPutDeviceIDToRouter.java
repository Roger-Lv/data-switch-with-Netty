package org.bdware.sw.server.putKV;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.cluster.client.DOAConfigBuilder;
import org.bdware.doip.cluster.client.DoaClusterClient;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.sw.SM3Tool;
import org.junit.Test;
import org.zz.gmhelper.SM2KeyPair;
import org.zz.gmhelper.SM2Util;

import java.io.FileInputStream;
import java.util.Scanner;


public class BatchPutDeviceIDToRouter {

    static Logger LOGGER = LogManager.getLogger(BatchPutDeviceIDToRouter.class);
    String address = "tcp://8.130.140.101:21030";
    @Test
    public void putGatewayAndSwitch() throws Exception {
        putGateway();
        putSwitch();
    }
    @Test
    public void putGateway() throws Exception {
        putKV("./ContractDB/gateway-dol.json");
    }

    @Test
    public void putSwitch() throws Exception {
        putKV("./ContractDB/switch-dol.json");
    }

    @Test
    public void printMap() throws Exception {
        printKeyAndDOL("./ContractDB/gateway-dol.json");
        printKeyAndDOL("./ContractDB/switch-dol.json");
    }

    private void printKeyAndDOL(String path) throws Exception {
        for (Scanner sc = new Scanner(new FileInputStream(path)); sc.hasNextLine(); ) {
            String content = sc.nextLine();
            Line line = new Gson().fromJson(content, Line.class);
            LOGGER.info(line.key + "-->" + SM3Tool.toSM3(line.key));
        }
    }

    static class Line {
        String key;
        String value;
    }

    public void putKV(String filePath) throws Exception {
//        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig(
//                "{\"type\":\"static\",\"content\":{\"version\":\"2.1\",\"address\":\"tcp://8.130.140.101:21054\"}}"));
        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig(
                "{\"type\":\"static\",\"content\":{\"version\":\"2.1\",\"address\":\"tcp://112.124.32.20:21054\"}}"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("whatever", BasicOperations.Update.getName());
        for (Scanner sc = new Scanner(new FileInputStream(filePath)); sc.hasNextLine(); ) {
            String content = sc.nextLine();
            Line line = new Gson().fromJson(content, Line.class);
            builder.addAttributes("dol", SM3Tool.toSM3(line.key));
            builder.addAttributes("type", "Entrance");
            if (!line.value.startsWith("tcp://"))
                line.value = "tcp://" + line.value;
            builder.addAttributes("address", line.value);
            DoipMessage response = client.sendMessageSync(builder.create(), 5000);
            LOGGER.info(response.header.parameters.attributes);
        }


    }

}

