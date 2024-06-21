package org.bdware.sw.monitor;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import org.bdware.sw.SWConfig;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MetricsForGrafana {
    private final Timer timer;
    // 指令计数, 在SwitchHandler中处理的doipMessage的计数
    private Counter doipCommandCounter;
    // 数据计数，在DOHandler中处理的doipMessage的计数
    private Counter doipDataCounter;
    // 数据计数，在DOHandler中处理的doipMessage的错误计数
    private Counter doipWrongDataCounter;
    private Counter heartbeatCounter; //这个的作用是?
    // 接收到的字节数
    private Counter networkReceiveBytes;
    // 转发的字节数
    private Counter networkTransmitBytes;

    HTTPServer server;
    int currentPort;
    SWConfig swConfig;

    public MetricsForGrafana() {
        doipCommandCounter = Counter.build()
                .name("doip_command_requests_total")
                .help("Total number of doip requests with command flag=1 and the id is equal to the switch id")
                .labelNames("deviceId", "operation")
                .register();
        doipDataCounter = Counter.build()
                .name("doip_data_requests_total")
                .help("Total number of doip requests")
                .labelNames("deviceId", "operation")
                .register();
        doipWrongDataCounter = Counter.build()
                .name("doip_data_wrong_requests")
                .help("Total number of doip requests")
                .labelNames("deviceId", "operation")
                .register();
        heartbeatCounter = Counter.build()
                .name("heartbeat_total")
                .labelNames("deviceId")
                .help("Total number of heartbeats")
                .register();
        // label: total, command, switch, space
        networkReceiveBytes = Counter.build()
                .name("doip_receive_bytes")
                .help("Total number of received network bytes")
                .labelNames("deviceId", "interface") //interface表明对象的类型（label: total, command, switch, space）
                .register();
        networkTransmitBytes = Counter.build()
                .name("doip_transmit_bytes")
                .help("Total number of transmitted network bytes")
                .labelNames("deviceId", "interface")
                .register();
        DefaultExports.initialize();
        timer = new Timer();
        this.swConfig = SWConfig.globalConfig;
        try {
            currentPort = this.swConfig.prometheusPort;
            server = new HTTPServer(this.swConfig.prometheusPort);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    heartbeatCounter.labels(MetricsForGrafana.this.swConfig.id).inc();
                }
            }, 0L, 10000L); // 1000 毫秒表示1秒钟
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restartServer() {
        if (swConfig.prometheusPort == currentPort) return;
        if (server != null) server.close();
        try {
            swConfig = SWConfig.globalConfig;
            server = new HTTPServer(swConfig.prometheusPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 下面的都会空指针报错
    public Counter.Child doipCommand(String operation) {
        return doipCommandCounter.labels(swConfig.id, operation);
    }

    public Counter.Child doipData(String operation) {
        return doipDataCounter.labels(swConfig.id, operation);
    }

    public Counter.Child doipWrongDataCounter(String operation) {
        return doipWrongDataCounter.labels(swConfig.id, operation);
    }

    public Counter.Child networkReceive(String tag) {
        return networkReceiveBytes.labels(swConfig.id, tag);
    }

    public Counter.Child networkTransmit(String tag) {
        return networkTransmitBytes.labels(swConfig.id, tag);
    }


}
