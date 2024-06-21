package org.bdware.sw.handler;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.pool.FixedChannelPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.doipMessage.DoipResponseCode;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.bdware.doip.endpoint.client.ResponseWait;
import java.util.Random;

@ChannelHandler.Sharable
public class NettyDoipPooledClientHandler extends SimpleChannelInboundHandler<DoipMessage> {
    static Logger logger = LogManager.getLogger(NettyDoipPooledClientHandler.class);
    ResponseWait sync = new ResponseWait();
    Random random = new Random();


    public void sendMessage(DoipMessage request, Channel channel,DoipMessageCallback callback) {
        sendMessage(request, channel,callback, 10);
    }

    public void sendMessage(DoipMessage request, Channel channel,DoipMessageCallback callback, int timeoutSeconds) {
        if (callback == null) {
            logger.error("DoipMessageCallback is null, please check!");
            return;
        }

        int retryCount = 0;

        if (request.requestID == 0) {
            request.requestID = random.nextInt();
        }

        int MAX_RETRY_COUNT = 10;
        while (retryCount < MAX_RETRY_COUNT && !sync.waitResponse(request.requestID, callback, timeoutSeconds)) {
            request.requestID = random.nextInt();
            Thread.yield();
            retryCount++;
        }
        // logger.debug("writeAndFlush: " + new String(request.header.parameters.toByteArray()));
        // logger.debug("channel status: " + channel.isActive());

        if (retryCount >= MAX_RETRY_COUNT) {
            logger.error("waitObj.size() is too large! Could not get response: " + request.requestID);
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createResponse(DoipResponseCode.MoreThanOneErrors, request);
            builder.addAttributes("msg", "waitObj.size too large!");
            callback.onResult(builder.create());
        } else {
            channel.writeAndFlush(request);
        }

    }


    public void close(Channel channel) {
        channel.close();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DoipMessage msg) {
        logger.debug("channelRead0 receive a message");
        if (msg.header.parameters.attributes != null && msg.header.parameters.attributes.get("action") != null) {
            if (msg.header.parameters.attributes.get("action").getAsString().equals("start")) {
                sync.wakeup(msg.requestID, msg);
            } else {
                logger.debug("stop stream");
                sync.wakeUpAndRemove(msg.requestID, msg);
            }
        } else {
            sync.wakeUpAndRemove(msg.requestID, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getMessage());
        if (ctx.channel().id() != null && SimpleChannelPooledHandler.ioHandlerMap.containsKey(ctx.channel().id())) {
            SimpleChannelPooledHandler.ioHandlerMap.remove(ctx.channel().id());
        }
        ctx.close();
    }


}

