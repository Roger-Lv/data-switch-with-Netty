import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipClientImpl;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.bdware.sw.SM3Tool;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DoipRequestForSpace {
    static Logger logger = LogManager.getLogger(DoipRequestForSpace.class);

    @Test
    public void sm3() {
        String id = "https://github.com/HKUST-Aerial-Robotics/ESVO2";
        logger.info(SM3Tool.toSM3(id));
    }

    @Test
    public void run() throws InterruptedException {
        DoipClientImpl clientForSwitch = new DoipClientImpl();
        ClientConfig config = new ClientConfig("tcp://8.130.136.43:21060");
        clientForSwitch.connect(config);

        // 使用 ExecutorService 来管理线程池
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        int numOfTask = 1;
        final int[] finishNum = {0};
        // 记录任务开始时间
        long startTime = System.currentTimeMillis();
        final long[] totalDuration = {0};
        // 启动多个线程执行任务
        CountDownLatch countDownLatch = new CountDownLatch(1);
        for (int i = 0; i < numOfTask; i++) {
            int threadNum = i; // 记录当前线程的序号
            if (threadNum == 1) Thread.sleep(2);
            executorService.submit(() -> {
                long threadStartTime = System.currentTimeMillis();
                try {
                    DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
                    builder.createRequest("CSTR:18406.11.HYDRO.TPDC.270098", BasicOperations.Retrieve.getName());
                    DoipMessage request = builder.create();
                    clientForSwitch.sendMessage(request, new DoipMessageCallback() {
                        @Override
                        public void onResult(DoipMessage msg) {
                            logger.info("client 已经接收到回调：" + msg.body.getDataAsJsonString() + " " + msg.header.parameters.attributes);
                            long threadEndTime = System.currentTimeMillis();
                            long threadDuration = threadEndTime - threadStartTime;
                            totalDuration[0] += threadDuration;
                            System.out.println("Thread " + threadNum + " completed in " + threadDuration + "ms");
                            finishNum[0] += 1;
                            if (finishNum[0] == numOfTask) {
                                // 记录任务结束时间
                                long endTime = System.currentTimeMillis();
                                // 打印总时间和每个线程任务的平均完成时间
                                System.out.println("Total time: " + totalDuration[0] + "ms");
                                System.out.println("Average time per thread: " + (totalDuration[0] / numOfTask) + "ms");
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        for (; countDownLatch.getCount() > 0; ) Thread.yield();


    }


    @Test
    public void retrieveLocal() throws InterruptedException {
        DoipClientImpl clientForSwitch = new DoipClientImpl();
        ClientConfig config = new ClientConfig("tcp://127.0.0.1:18060");
       config = new ClientConfig("tcp://8.130.140.101:21060");
      //     config = new ClientConfig("tcp://127.0.0.1:18060");
        clientForSwitch.connect(config);
        AtomicInteger countDownLatch = new AtomicInteger(0);
        String id = "CSTR:18406.11.HYDRO.TPDC.270098";

        id = "10.48550/arXiv.1805.10616";
             id = "ids:dataset/d7cfbf4e41217d8c8d9931057c557a87b5b49e6367da82e8746ddf9b10a0cc5f";
        //     id = "CSTR:18406.11.HYDRO.TPDC.270098";
        //   id = "https://github.com/HKUST-Aerial-Robotics/ESVO2";

        try {
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createRequest(id, BasicOperations.Retrieve.getName());
            DoipMessage request = builder.create();
            long threadStartTime = System.currentTimeMillis();
            clientForSwitch.sendMessage(request, new DoipMessageCallback() {
                @Override
                public void onResult(DoipMessage msg) {
                    long threadEndTime = System.currentTimeMillis();
                    long threadDuration = threadEndTime - threadStartTime;
                    logger.info("client 已经接收到回调：" + msg.body.getDataAsJsonString() + " " + msg.header.parameters.attributes + " dur:" + threadDuration);
                    countDownLatch.incrementAndGet();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (; countDownLatch.get() == 0; ) Thread.yield();
    }

    @Test
    public void knowIP() {
        DoipClientImpl client = new DoipClientImpl();
        client.connect(ClientConfig.fromUrl("tcp://127.0.0.1:18051"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
//        builder.createRequest("bdtest/Repository/0bed75b1-ec2b-41a0-96f6-a03916b7a995", BasicOperations.Retrieve.getName());
        builder.createRequest("86.5000.470/do.hello", BasicOperations.Retrieve.getName());
        builder.addAttributes("onlineFilePath", "testOnlineFilePath");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.sendMessage(builder.create(), new DoipMessageCallback() {
            @Override
            public void onResult(DoipMessage msg) {
                logger.info(msg.body.getDataAsJsonString());
            }
        });
        for (; countDownLatch.getCount() > 0; ) Thread.yield();

    }
}
