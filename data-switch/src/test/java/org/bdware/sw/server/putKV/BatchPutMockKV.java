package org.bdware.sw.server.putKV;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.client.SmartContractHttpClient;
import org.junit.Test;
import org.zz.gmhelper.SM2KeyPair;
import org.zz.gmhelper.SM2Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BatchPutMockKV {

    static Logger LOGGER = LogManager.getLogger(BatchPutMockKV.class);
    String address = "tcp://8.130.140.101:21030";

    @Test
    public void putDoi() throws Exception {
        SM2KeyPair pair = SM2Util.generateSM2KeyPair();
        SmartContractHttpClient client = new SmartContractHttpClient(pair, "8.130.140.101", 21030, "POST");
        String filePath = "D:/study/research/IoD-switch/data-switch/src/test/java/org/bdware/sw/server/puredoi.json";
        ObjectMapper mapper = new ObjectMapper();
        List<String> idList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode node = mapper.readTree(line);
                String id = node.get("id").textValue();
                LOGGER.info("id: " + id);
                idList.add(id);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("size:"+idList.size());
        for (String id:idList){
            //doi->left switch
            JsonObject result1 = client.executeContract("MockKV", "put", "{\"key\":\"" + id + "\",\"value\":\"127.0.0.1:2042\"}");
            LOGGER.info(result1.toString());
        }

    }

    @Test
    public void putGit() throws Exception {
        SM2KeyPair pair = SM2Util.generateSM2KeyPair();
        SmartContractHttpClient client = new SmartContractHttpClient(pair, "8.130.140.101", 21030, "POST");
        String filePath = "D:/study/research/IoD-switch/data-switch/src/test/java/org/bdware/sw/server/puregithubid.json";
        ObjectMapper mapper = new ObjectMapper();
        List<String> idList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode node = mapper.readTree(line);
                String id = node.get("id").textValue();
                LOGGER.info("id: " + id);
                idList.add(id);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("size:"+idList.size());
        for (String id:idList){
            //git->left switch
            JsonObject result1 = client.executeContract("MockKV", "put", "{\"key\":\"" + id + "\",\"value\":\"127.0.0.1:2042\"}");
            LOGGER.info(result1.toString());
        }

    }

    @Test
    public void get() throws Exception {
        SM2KeyPair pair = SM2Util.generateSM2KeyPair();
        SmartContractHttpClient client = new SmartContractHttpClient(pair, "8.130.140.101", 21030, "POST");
        //github
        JsonObject result = client.executeContract("MockKV", "get", "{\"key\":\"GitHub:saved\"}");
        LOGGER.info(result.toString());
        //solid
        JsonObject result2 = client.executeContract("MockKV", "get", "{\"key\":\"Solid:86.5000.470/do.hello\"}");
        LOGGER.info(result2.toString());
    }
}

