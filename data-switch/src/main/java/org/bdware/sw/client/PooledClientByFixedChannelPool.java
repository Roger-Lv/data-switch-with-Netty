package org.bdware.sw.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.bdware.sw.channel.NettyClientPooledChannel;
import org.bdware.sw.monitor.MetricsForGrafana;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class PooledClientByFixedChannelPool {

    static Logger logger = LogManager.getLogger(PooledClientByFixedChannelPool.class);


    private NettyClientPooledChannel channels;
    String serverURL = null;

    public PooledClientByFixedChannelPool(ClientConfig config, int maxConnections, MetricsForGrafana metrics) throws URISyntaxException, InterruptedException, MalformedURLException {
        this.channels = new NettyClientPooledChannel(config, maxConnections, metrics);
        this.serverURL = config.url;
    }

    public void sendMessage(DoipMessage msg, DoipMessageCallback cb) throws Exception {
        channels.sendMessage(msg, cb);
    }
}
