package org.bdware.sw.server.clientForSwitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.bdware.sw.client.PooledClientByFixedChannelPool;
import org.bdware.sw.monitor.MetricsForGrafana;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class ClientForSwitchTest2 {
    static Logger logger = LogManager.getLogger(ClientForSwitchTest2.class);

    public static void main(String[] args) throws Exception {
        String filePath = "data-switch/src/test/java/org/bdware/sw/server/peretoWindowRandomIdList.json";

        CountDownLatch countDownLatch = new CountDownLatch(1);
        List<String> idList = getIdList(filePath);
        int port = 2042;

//        ClusterClientForSwitch client= new ClusterClientForSwitch(100,config,new MetricsForGrafana(config));
//        ClusterClientForSwitch client= new ClusterClientForSwitch();
        PooledClientByFixedChannelPool client = new PooledClientByFixedChannelPool(ClientConfig.fromUrl("tcp://" + "127.0.0.1" + ":" + port), 100, new MetricsForGrafana());

//        DoipClientImpl client = new DoipClientImpl();
//        client.connect(ClientConfig.fromUrl("tcp://" + "127.0.0.1" + ":" + port ));
        // 使用 ExecutorService 来管理线程池
        ExecutorService executorService = Executors.newFixedThreadPool(8);
//        int numOfTask = Math.min(700000,idList.size());
        int numOfTask = 500000;

        // 启动多个线程执行任务
        //预热
//        for (int i = 0; i < 1000; i++) {
//            int threadNum = i; // 记录当前线程的序号
//            int finalI = i;
//            executorService.submit(() -> {
//                long threadStartTime = System.currentTimeMillis();
//                try {
//                    DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
//                    builder.createRequest("https://github.com/johnnlp/saved", BasicOperations.Retrieve.getName());
//                    if (finalI==0)
////                    startList.add(finalI,startTime);
//                    //"127.0.0.1",2042,
//                    client.sendMessage(builder.create(),new DoipMessageCallback() {
//                        @Override
//                        public void onResult(DoipMessage msg) {
//                            double endTime = System.currentTimeMillis();
//
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                long threadEndTime = System.currentTimeMillis();
//                long threadDuration = threadEndTime - threadStartTime;
//                System.out.println("Thread " + threadNum + " completed in " + threadDuration + "ms");
//            });
//        }
//        Thread.sleep(4000);

        AtomicReference<Long> sstartTime = new AtomicReference<Long>();
        AtomicReference<Long> lstartTime = new AtomicReference<Long>();
        AtomicReference<Long> eendTime = new AtomicReference<Long>();
        sstartTime.set(Integer.toUnsignedLong(0));
        lstartTime.set(Integer.toUnsignedLong(0));
        eendTime.set(Integer.toUnsignedLong(0));
        AtomicLong sumOfCostTime = new AtomicLong(0);
        AtomicLong outOfTimeNum = new AtomicLong(0);
        List<Integer> listOutOfTime = new ArrayList<>();
        for (int i = 0; i < numOfTask + 20; i++) {
            int threadNum = i; // 记录当前线程的序号
            int finalI = i;
            Thread.sleep(10);
//            if (finalI%10000==0)Thread.sleep(700);
            if (finalI < numOfTask) {
                executorService.submit(() -> {
                    long threadStartTime = System.nanoTime();
                    Long startTime = System.currentTimeMillis();
                    try {

                        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
//                        builder.createRequest("https://github.com/johnnlp/saved", BasicOperations.Retrieve.getName());
                        builder.createRequest(idList.get(finalI), BasicOperations.Retrieve.getName());
                        if (finalI == 0)
                            sstartTime.set(System.currentTimeMillis());
                        if (finalI == numOfTask - 1)
                            lstartTime.set(System.currentTimeMillis());
                        //"127.0.0.1",2042,

                        client.sendMessage(builder.create(), new DoipMessageCallback() {
                            @Override
                            public void onResult(DoipMessage msg) {
                                Long endTime = System.currentTimeMillis();

                                sumOfCostTime.addAndGet((int) (endTime - startTime));
                                if (msg.header.parameters.id == null && !msg.body.getDataAsJsonString().startsWith("unsupported for:")) {
                                    outOfTimeNum.addAndGet(1);
                                    listOutOfTime.add(finalI);
                                }

                                if (endTime > eendTime.get()) eendTime.set(endTime);
                                logger.info("ClientForSwitch耗时：" + (endTime - startTime) + "ms");
//                                logger.info("全部线程完成总耗时: "+(eendTime.get()-sstartTime.get())+"ms");
//                                logger.info("请求总耗时: "+sumOfCostTime.get()+"ms");
//                                logger.info("超时条目/总条目: "+outOfTimeNum.get()+"/"+numOfTask);
//                                logger.info("平均每条请求耗时："+sumOfCostTime.get()/finalI+"ms");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    long threadEndTime = System.currentTimeMillis();
                    long threadDuration = threadEndTime - threadStartTime;
                    System.out.println("Thread " + threadNum + " completed ");
                });
            } else {
                Thread.sleep(15000);
                logger.info("全部线程完成总耗时: " + (eendTime.get() - sstartTime.get()) + "ms");
                logger.info("线程启动耗时: " + (lstartTime.get() - sstartTime.get()) + "ms");
                double time = (lstartTime.get() - sstartTime.get()) / 1000;
                int numOfTask1 = numOfTask;
                double num = numOfTask1;
                double sumTime = sumOfCostTime.get();
                logger.info("并发量: " + (num / time) + "/s");
                logger.info("请求总耗时: " + sumOfCostTime.get() + "ms");
                logger.info("超时条目/总条目: " + outOfTimeNum.get() + "/" + numOfTask);
                logger.info("平均每条请求耗时：" + sumTime / numOfTask + "ms");
                logger.info("超时列表 : " + listOutOfTime);

            }

        }
        for (; countDownLatch.getCount() > 0; ) Thread.yield();

    }

    public static List<String> getIdList(String filePath) {
        List<String> idList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("\t", "");
                JsonNode node = mapper.readTree(line);
                String id = node.get("id").textValue();
                if (id.startsWith("https://github.com"))
                    idList.add(id);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("size of list:" + idList.size());
        return idList;
    }


}
