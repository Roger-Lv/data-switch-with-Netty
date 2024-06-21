package org.bdware.sw.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.cluster.client.DOAConfigBuilder;
import org.bdware.doip.cluster.client.DoaClusterClient;
import org.bdware.sw.client.clusterclient.ClusterClient;

public class ClientForDigitalSpace extends DoaClusterClient implements ClusterClient {
    //Key=doipmessage的请求的哈希值
    // static Cache<String,DoipResponse>  = Caffeine.newBuilder().
    static Logger LOGGER = LogManager.getLogger(ClientForDigitalSpace.class);

    public ClientForDigitalSpace(String irpURL) {
        super(DOAConfigBuilder.withIrpConfig(irpURL));
    }

    static class DoipResponse {

    }
}


