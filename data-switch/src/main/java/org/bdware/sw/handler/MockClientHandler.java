package org.bdware.sw.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.bdware.doip.codec.doipMessage.DoipMessage;
@ChannelHandler.Sharable
public class MockClientHandler extends SimpleChannelInboundHandler<DoipMessage>
{

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DoipMessage msg)  throws Exception
    {

        String response = "你好，我是MockClient，我已经收到DO数据：" + msg.body.getDataAsJsonString();
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

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        System.err.println("MockClient读取DO完毕");
//        ctx.channel().close();
//    }
}

