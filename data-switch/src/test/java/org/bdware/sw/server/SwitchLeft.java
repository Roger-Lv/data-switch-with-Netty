package org.bdware.sw.server;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.sw.DataSwitch;
import org.bdware.sw.IrpDetector;
import org.bdware.sw.SwitchManagementIntf;
import org.bdware.sw.builder.DataSwitchBuilder;
import org.bdware.sw.builder.DataSwitchTCPBuilder;
import org.bdware.sw.director.DirectorForDataSwitch;
import org.bdware.sw.nodemanager.AddressOfSwitch;
import org.bdware.sw.statistics.Statistics;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class SwitchLeft {
    static Logger LOGGER = LogManager.getLogger(SwitchLeft.class);

    public static void runLeft() throws Exception {
        String leftSwitchHost = "127.0.0.1";
        int leftSwitchPort = 2042;
        String leftSwitchId = "leftSwtich999999";
        EndpointConfig endpointConfig = new EndpointConfig();
        //GitHub
//        endpointConfig.routerURI = "tcp://8.130.136.43:21041";
        //IDS
        endpointConfig.routerURI = "tcp://120.46.79.34:21041";
        List<IrpDetector.StaticRouteEntry> entryList = new ArrayList<>();
        //配置metrics
//        ProxyConfig config = new ProxyConfig();
//        config.id = "testleft1";
        DataSwitchBuilder builder = new DataSwitchTCPBuilder();
        DirectorForDataSwitch director = new DirectorForDataSwitch(builder);
        DataSwitch leftSwitch =director.constructDataSwitch(leftSwitchHost, leftSwitchPort, new SwitchManagementIntf() {
            @Override
            public String updateConfig(JsonObject updateItem) {
                return "not supported";
            }
        }, endpointConfig, leftSwitchId, entryList, () -> {
            //这里暂时不publish
        });
        leftSwitch.nettyDataSwitchListener.dispatcher.switchNodeManager.addAddressCache("https://github.com/Jencke/binaural-detection-mod2222test", new AddressOfSwitch("right", "127.0.0.1", 2043));

//        leftSwitch.nettyDataSwitchListener.start();

        // 使用 ExecutorService 来管理线程池
        ExecutorService executorService = Executors.newFixedThreadPool(150);
        int numOfTask = 1000;
        // 启动多个线程执行任务
        AtomicLong totalDuration = new AtomicLong();
        for (int i = 0; i < numOfTask; i++) {

            if (i == 0) {
                executorService.submit(() -> {
                    leftSwitch.nettyDataSwitchListener.start();

                });
                Thread.sleep(10000);
            } else
                executorService.submit(() -> {
                    Statistics statistics = leftSwitch.getStatistics();
                    System.out.println("doipOp: " + statistics.doipOp);
                    System.out.println("doCount: " + statistics.doCount);
                    System.out.println("dpCount: " + statistics.dpCount);
                });
            Thread.sleep(10000);
        }

    }

    public static void main(String[] args) throws Exception {
        runLeft();
    }

}
