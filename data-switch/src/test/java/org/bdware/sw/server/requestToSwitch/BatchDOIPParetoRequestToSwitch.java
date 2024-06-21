package org.bdware.sw.server.requestToSwitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.sw.client.MockUserClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BatchDOIPParetoRequestToSwitch {
    public static void main(String[] args) throws Exception {
        String filePath = "data-switch/src/test/java/org/bdware/sw/server/puregithubid.json";
        List<String> idList = getIdList(filePath);
        idList = idList.subList(0,1000);
        float size = idList.size();
        System.out.println("size："+size);
//        int leftSwitchPort=2042;
        int rightSwitchPort=2043;
        runMockClient(idList,rightSwitchPort);



    }
    public static List<String> getIdList(String filePath){
        List<String> idList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode node = mapper.readTree(line);
                String id = node.get("id").textValue();
                if (id.startsWith("https://github.com/"))
                idList.add(id);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("size of list:"+idList.size());
        return idList;
    }
    public static void runMockClient(List list,int port) throws Exception{

        //MockClient
        MockUserClient mockUserClient = new MockUserClient();


        // 使用 ExecutorService 来管理线程池
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        int numOfTask = list.size();
        AtomicInteger effectNum= new AtomicInteger(numOfTask);
        AtomicInteger sumOfCostTime = new AtomicInteger();
        // 启动多个线程执行任务
        AtomicLong totalDuration = new AtomicLong();
        for (int i = 0; i < numOfTask; i++) {
            int threadNum = i; // 记录当前线程的序号
//            Thread.sleep(200);
            //doipMessage
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createRequest((String) list.get(i), BasicOperations.Retrieve.getName());
            executorService.submit(() -> {
                long threadStartTime = System.currentTimeMillis();
                try {
                    int current = mockUserClient.sendMessage(builder.create(), "127.0.0.1", port);
                    if (current>1000){
                        effectNum.decrementAndGet();

                    }else{
                        sumOfCostTime.addAndGet(current);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long threadEndTime = System.currentTimeMillis();
                long threadDuration = threadEndTime - threadStartTime;
                totalDuration.addAndGet(threadDuration);
                System.out.println("Thread " + threadNum + " completed in " + threadDuration + "ms");
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

        // 打印总时间和每个线程任务的平均完成时间
        System.out.println("Total time: " + sumOfCostTime + "ms");
        System.out.println("Average time per thread: " + (sumOfCostTime.get() / effectNum.get()) + "ms");
    }

}
