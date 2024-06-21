package org.bdware.sw.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipClientImpl;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.bdware.sw.config.ServerEndpoint;
import org.bdware.sw.nodemanager.cache.LRU.MyLRUCache;

public class ClientForSwitch {

    public static MyLRUCache<ServerEndpoint,DoipClientImpl> lruCache= new MyLRUCache<>(100000);;
    static Logger logger = LogManager.getLogger(ClientForSwitch.class);

    public ClientForSwitch() {

    }

    public boolean isStored(ServerEndpoint endpoint){
        synchronized (lruCache){
            if (this.lruCache.get(endpoint)!=null)return true;
            return false;
        }
    }

    public void addClient(ServerEndpoint endpoint,DoipClientImpl client){
        this.lruCache.put(endpoint,client);
    }

    public void sendMessage(DoipMessage msg, String host, int port, DoipMessageCallback callback) throws Exception {
        ServerEndpoint endpoint=new ServerEndpoint(host,port);
        //如果之前已经存过了
        if (isStored(endpoint)){
            logger.info("isStored!Reuse the client created before.");
            lruCache.get(endpoint).sendMessage(msg,callback);
        }else{
            logger.info("Create new client!");
            DoipClientImpl client = new DoipClientImpl();
            client.connect(ClientConfig.fromUrl("tcp://" + host + ":" + port));
            client.sendMessage(msg, callback);
            addClient(endpoint,client);
        }
    }
}
