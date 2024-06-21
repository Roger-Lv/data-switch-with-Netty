package org.bdware.sw.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.sw.monitor.MetricsForGrafana;

import java.util.List;

@ChannelHandler.Sharable
public class NetworkTrafficStatHandler extends MessageToMessageCodec<ByteBuf, ByteBuf> {
    static Logger LOGGER = LogManager.getLogger(NetworkTrafficStatHandler.class);
    MetricsForGrafana metrics;

    public NetworkTrafficStatHandler(MetricsForGrafana metrics) {
        this.metrics = metrics;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        //发出的网络包大小。
        metrics.networkTransmit("total").inc(msg.readableBytes());
        out.add(msg.retain());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        //收到的网络包大小。
        metrics.networkReceive("total").inc(msg.readableBytes());
        out.add(msg.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.debug("got exception: " + cause.getMessage());
        cause.printStackTrace();
        if (ctx.channel().isActive()) ctx.close();
    }
}
