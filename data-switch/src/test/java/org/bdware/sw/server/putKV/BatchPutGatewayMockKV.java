package org.bdware.sw.server.putKV;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.client.SmartContractHttpClient;
import org.bdware.sw.SM3Tool;
import org.junit.Test;
import org.zz.gmhelper.SM2KeyPair;
import org.zz.gmhelper.SM2Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BatchPutGatewayMockKV {

    static Logger LOGGER = LogManager.getLogger(BatchPutGatewayMockKV.class);
    String address = "tcp://8.130.140.101:21030";

    @Test
    public void putGateway() throws Exception {
        putKV("./ContractDB/gateway-dol.json");
    }

    @Test
    public void putSwitch() throws Exception {
        putKV("./ContractDB/switch-dol.json");
    }

    public void putKV(String filePath) throws Exception {
        SM2KeyPair pair = SM2Util.generateSM2KeyPair();
        SmartContractHttpClient client = new SmartContractHttpClient(pair, "8.130.140.101", 21030, "POST");
        List<JsonObject> idList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                JsonObject jo = JsonParser.parseString(line).getAsJsonObject();
                idList.add(jo);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("size:" + idList.size());
        for (JsonObject request : idList) {
            String key = request.get("key").getAsString();
            String sm3Dol = SM3Tool.toSM3(key);
            request.addProperty("key", sm3Dol);
            //git->left switch
            JsonObject result1 = client.executeContract("MockKV", "put", request.toString());
            LOGGER.info(result1.toString());
        }

    }

}

