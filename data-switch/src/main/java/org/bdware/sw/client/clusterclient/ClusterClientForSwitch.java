package org.bdware.sw.client.clusterclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.bdware.sw.client.PooledClientByFixedChannelPool;
import org.bdware.sw.config.ServerEndpoint;
import org.bdware.sw.monitor.MetricsForGrafana;

import java.util.concurrent.ConcurrentHashMap;

public class ClusterClientForSwitch {

    public static ConcurrentHashMap<ServerEndpoint, PooledClientByFixedChannelPool> clientForSwitchConcurrentHashMap = new ConcurrentHashMap<>(20);
    //    public static ConcurrentHashMap<ServerEndpoint, PooledClient> clientForSwitchConcurrentHashMap = new ConcurrentHashMap<>();
    static Logger logger = LogManager.getLogger(ClusterClientForSwitch.class);
    int MAX_CONNECTIONS;
    public MetricsForGrafana metrics;

    public ClusterClientForSwitch(int maxConnections, MetricsForGrafana metrics) {
        this.MAX_CONNECTIONS = maxConnections;
        this.metrics = metrics;
    }

    public boolean isStored(ServerEndpoint endpoint) {
        if (this.clientForSwitchConcurrentHashMap.get(endpoint) != null) return true;
        return false;
    }

    public void addClient(ServerEndpoint endpoint, PooledClientByFixedChannelPool client) {
        this.clientForSwitchConcurrentHashMap.put(endpoint, client);
    }

    public void sendMessage(DoipMessage msg, String host, int port, DoipMessageCallback callback) throws Exception {
        ServerEndpoint endpoint = new ServerEndpoint(host, port);

        //如果之前已经存过了
        if (isStored(endpoint)) {
//                logger.info("isStored!Reuse the client created before.");
            clientForSwitchConcurrentHashMap.get(endpoint).sendMessage(msg, callback);
        } else {
            PooledClientByFixedChannelPool client = new PooledClientByFixedChannelPool(ClientConfig.fromUrl("tcp://" + host + ":" + port), MAX_CONNECTIONS, metrics);
//                    PooledClient client = new PooledClient(ClientConfig.fromUrl("tcp://" + host + ":" + port), MAX_CONNECTIONS);
            addClient(endpoint, client);
            client.sendMessage(msg, callback);
        }

    }
}

