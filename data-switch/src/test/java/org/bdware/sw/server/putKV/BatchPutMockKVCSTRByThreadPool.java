package org.bdware.sw.server.putKV;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.client.SmartContractHttpClient;
import org.bdware.sw.SM3Tool;
import org.zz.gmhelper.SM2KeyPair;
import org.zz.gmhelper.SM2Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BatchPutMockKVCSTRByThreadPool {
    static Logger LOGGER = LogManager.getLogger(BatchPutMockKVDoiByThreadPool.class);


    public static void putCSTR() throws Exception {
        SM2KeyPair pair = SM2Util.generateSM2KeyPair();
        SmartContractHttpClient client = new SmartContractHttpClient(pair, "8.130.140.101", 21030, "POST");
        String filePath = "D:/study/research/IoD-switch/data-switch/src/test/java/org/bdware/sw/server/puredoi.json";
        filePath = "data-switch/src/test/java/org/bdware/sw/server/casdatacenter(1).jsonl";
        ObjectMapper mapper = new ObjectMapper();
        List<String> idList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode node = mapper.readTree(line);
                JsonNode node1 = node.get("desc");
                if (node1.get("cstr")==null)continue;
                String id=node1.get("cstr").textValue();
                if (id==null)continue;
                if (id.startsWith("CSTR:")){
                    LOGGER.info("id:" + id);
                    idList.add(id);
                }else if (id.startsWith("https://cstr.cn/")){
                    id=id.replaceAll("https://cstr.cn/","CSTR:");
                    LOGGER.info("idAfterReplace: " + id);
                    idList.add(id);
                }else if(id.startsWith("cstr:")){
                    id=id.replaceAll("cstr:","CSTR:");
                    LOGGER.info("idAfterReplace: " + id);
                    idList.add(id);
                }

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("size:" + idList.size());

        // 使用 ExecutorService 来管理线程池
        ExecutorService executorService = Executors.newFixedThreadPool(80);
        int threadNum = 0;
        for (String id : idList) {
            threadNum += 1;
            //doi->left switch
            int finalThreadNum = threadNum;
            executorService.submit(() -> {
                long threadStartTime = System.currentTimeMillis();
                try {
                    String sm3Id = SM3Tool.toSM3(id);
                    JsonObject result1 = client.executeContract("MockKV", "put", "{\"key\":\"" + sm3Id + "\",\"value\":\"8.130.140.101:21060\"}");
                    LOGGER.info(result1.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long threadEndTime = System.currentTimeMillis();
                long threadDuration = threadEndTime - threadStartTime;

                System.out.println("Thread " + finalThreadNum + " completed in " + threadDuration + "ms");
            });

        }
        // 关闭线程池
        executorService.shutdown();

        try {
            // 等待所有任务完成，最多等待1小时
            if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                // 在超时后仍有任务未完成，强制关闭
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // 处理中断异常
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws Exception {
        putCSTR();
    }
}
