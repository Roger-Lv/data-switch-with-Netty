package org.bdware.sw.server.requestToSwitch;

import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.sw.client.MockUserClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MockUserToRightSwitch {
    public static void runMockClient() throws Exception{
        int rightSwitchPort=2043;
        //MockClient
        MockUserClient mockUserClient = new MockUserClient();

        //构造请求，预热ClientForSwitch
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("https://github.com/maxfischer2781/bootpeg", BasicOperations.Retrieve.getName());
        mockUserClient.sendMessage(builder.create(),"127.0.0.1",rightSwitchPort);

        // 使用 ExecutorService 来管理线程池
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        // 总耗时
        AtomicInteger sumCostTime = new AtomicInteger();
        //任务数量
        int numOfTask = 1000;
        // 启动多个线程执行任务
        AtomicLong totalDuration = new AtomicLong();
        for (int i = 0; i < numOfTask; i++) {
            int threadNum = i; // 记录当前线程的序号
            executorService.submit(() -> {
                long threadStartTime = System.currentTimeMillis();
                try {
                    //doipMessage
                    DoipMessageFactory.DoipMessageBuilder builder2 = new DoipMessageFactory.DoipMessageBuilder();
                    builder2.createRequest("https://github.com/johnnlp/saved", BasicOperations.Retrieve.getName());
                    sumCostTime.addAndGet(mockUserClient.sendMessage(builder2.create(), "127.0.0.1",rightSwitchPort));
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
//        System.out.println("Total time: " + totalDuration + "ms");
        System.out.println("Total time: " + sumCostTime + "ms");
//        System.out.println("Average time per thread: " + (totalDuration.get() / numOfTask) + "ms");
        System.out.println("Average time per thread: " + (sumCostTime.get() / numOfTask) + "ms");
    }
    public static void main(String[] args)throws Exception{
        runMockClient();
    }
}
