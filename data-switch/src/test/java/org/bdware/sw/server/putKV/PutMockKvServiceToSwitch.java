package org.bdware.sw.server.putKV;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.client.SmartContractHttpClient;
import org.bdware.sw.SM3Tool;
import org.bdware.sw.server.putKV.BatchPutMockKVGitByThreadPool;
import org.zz.gmhelper.SM2KeyPair;
import org.zz.gmhelper.SM2Util;

public class PutMockKvServiceToSwitch {
    static Logger LOGGER = LogManager.getLogger(BatchPutMockKVGitByThreadPool.class);

    public static void put() throws Exception {
        SM2KeyPair pair = SM2Util.generateSM2KeyPair();
        SmartContractHttpClient client = new SmartContractHttpClient(pair, "8.130.140.101", 21030, "POST");

        String id = "https://github.com/Jencke/binaural-detection-mod2222test";

        String sm3Id = SM3Tool.toSM3(id);
        JsonObject result1 = client.executeContract("MockKV", "put", "{\"key\":\"" + sm3Id + "\",\"value\":\"8.130.140.101:21070\"}");
        LOGGER.info(result1.toString());


    }

    public static void main(String[] args)throws Exception{
        put();
    }
}
