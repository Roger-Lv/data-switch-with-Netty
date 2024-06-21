package org.bdware.sw.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.exception.DoipConnectException;
import org.bdware.doip.endpoint.EndpointFactory;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipClientChannel;
import org.bdware.doip.endpoint.client.DoipMessageCallback;

import java.net.URISyntaxException;
import java.util.Random;

public class PooledClient {

    static Logger logger = LogManager.getLogger(PooledClient.class);
    //channel数量大小
    int MAX_CHANNEL_COUNT;
    String recipientID;
    private DoipClientChannel[] channels;
    String serverURL = null;
    public PooledClient(ClientConfig  config, int MAX_CHANNEL_COUNT) throws URISyntaxException, InterruptedException {
        this.MAX_CHANNEL_COUNT = MAX_CHANNEL_COUNT;
        this.channels = new DoipClientChannel[MAX_CHANNEL_COUNT];
        this.serverURL = config.url;
        for (int i = 0; i < MAX_CHANNEL_COUNT; i++) {
            //初始化channel
            this.channels[i]=EndpointFactory.createDoipClientChannel(config);
//            logger.info("初始化DoipClientchannel，编号"+i+"成功");
//            //初始化连接
//            this.channels[i].connect(config.url);
//            logger.info("DoipClientchannel，编号"+i+"连接成功，url："+config.url);
        }
    }

    public DoipClientChannel syncGetChannel() throws InterruptedException {
        //产生一个随机数,随机的从数组中获取channel
        int index = new Random().nextInt(MAX_CHANNEL_COUNT);
        logger.info("this is channel id:"+index);
        DoipClientChannel channel = channels[index];
        //如果能获取到,直接返回
        return channel;
    }




    public void close(DoipClientChannel doipChannel) {
        doipChannel.close();
        logger.info("channel is closed");
    }

    public void reconnect(DoipClientChannel doipChannel) throws DoipConnectException {
        if (serverURL == null) throw (new DoipConnectException("target URL not set, use .connect(url) first"));
        ClientConfig clientConfig = ClientConfig.fromUrl(serverURL);
        if (doipChannel == null) doipChannel = EndpointFactory.createDoipClientChannel(clientConfig);
        if (doipChannel == null) return;
        try {
            doipChannel.connect(serverURL);
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected(DoipClientChannel doipChannel) {
        return doipChannel != null && doipChannel.isConnected();
    }


    public void sendMessage(DoipMessage msg, DoipMessageCallback cb) throws InterruptedException {
        //type org.bdware.doip.endpoint.client.NettyDoipTCPClientChannel
        DoipClientChannel doipChannel =syncGetChannel();
        if (!isConnected(doipChannel)) {
            if (!tryReconnect(doipChannel)) {
                logger.warn("channel not connect yet! " + (doipChannel == null) + " --> serverUrl:" + serverURL);
                DoipMessage resp = DoipMessageFactory.createConnectFailedResponse(msg.requestID);
                cb.onResult(resp);
                return;
            }
        }
        msg.setRecipientID(recipientID);
        doipChannel.sendMessage(msg, cb);
    }

    private boolean tryReconnect(DoipClientChannel doipChannel) {
        try {
            logger.info("channel reconnecting...");
            reconnect(doipChannel);
        } catch (DoipConnectException e) {
            throw new RuntimeException(e);
        }
        return isConnected(doipChannel);
    }

}
