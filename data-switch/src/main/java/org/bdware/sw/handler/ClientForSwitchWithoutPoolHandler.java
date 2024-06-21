package org.bdware.sw.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.endpoint.client.DoipMessageCallback;

public class ClientForSwitchWithoutPoolHandler extends SimpleChannelInboundHandler<DoipMessage> {
    DoipMessageCallback callback;
    public ClientForSwitchWithoutPoolHandler(DoipMessageCallback callback){
        this.callback=callback;
    }
    @Override
    public void channelRead0(ChannelHandlerContext ctx, DoipMessage msg)  throws Exception
    {

        String response = "你好，我是ClientForSwitchWithoutPoolHandler，我已经收到DO数据：" + msg.body.getDataAsJsonString();
        callback.onResult(msg);
        System.out.println(response);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        //出现异常的时候执行的动作（打印并关闭通道）
        cause.printStackTrace();
        ctx.channel().close();
    }
}
