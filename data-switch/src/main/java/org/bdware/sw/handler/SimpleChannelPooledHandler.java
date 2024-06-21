package org.bdware.sw.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.bdware.doip.codec.MessageEnvelopeAggregator;
import org.bdware.doip.codec.MessageEnvelopeCodec;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.sw.monitor.MetricsForGrafana;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleChannelPooledHandler extends AbstractChannelPoolHandler {
    /**
     * 缓存Channel与handler的映射关系
     */
    public static final Map<ChannelId, NettyDoipPooledClientHandler> ioHandlerMap = new ConcurrentHashMap<>();
    boolean splitEnvelop;
    int maxFrameLength;
    ClientConfig config;
    MetricsForGrafana metrics;

    public SimpleChannelPooledHandler(MetricsForGrafana metrics) {
        this.metrics = metrics;
    }

    @Override
    public void channelAcquired(Channel ch) throws Exception {
        super.channelAcquired(ch);
//        System.out.println("--------------------------------------------------------channelAcquired channel's id is:"+ch.id());
    }

    @Override
    public void channelReleased(Channel ch) throws Exception {
        super.channelReleased(ch);
//        System.out.println("--------------------------------------------------------channelReleased, id is:"+ch.id());
    }

    /**
     * 在创建ChannelPool连接池时会调用此方法对Channel进行初始化
     *
     * @param ch
     * @throws Exception
     */
    @Override
    public void channelCreated(Channel ch) throws Exception {
//        System.out.println("channel created. Channel ID: " + ch.id());
        //缓存当前Channel对应的handler
        ioHandlerMap.put(ch.id(), new NettyDoipPooledClientHandler());
        this.splitEnvelop = false;
        this.maxFrameLength = 5 * 1024 * 1024;
        ChannelPipeline p = ch.pipeline();
        p.addLast(new LengthFieldBasedFrameDecoder(maxFrameLength, 20, 4, 0, 0)).addLast(new NetworkTrafficStatForSwitchHandler(metrics)) //wrong 空指针
                .addLast(new MessageEnvelopeCodec()).addLast(new MessageEnvelopeAggregator(maxFrameLength - MessageEnvelope.ENVELOPE_LENGTH));

        //自定义handler处理
        p.addLast(new NettyDoipPooledClientHandler());

    }
}
