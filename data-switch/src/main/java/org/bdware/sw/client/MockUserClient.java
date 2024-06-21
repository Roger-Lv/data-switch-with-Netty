package org.bdware.sw.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.MessageEnvelopeAggregator;
import org.bdware.doip.codec.MessageEnvelopeCodec;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;
import org.bdware.sw.handler.MockClientHandler;

import java.net.InetSocketAddress;

public class MockUserClient {

    static Logger logger = LogManager.getLogger(MockUserClient.class);
    public MockUserClient() {

    }


    long threadDuration=0;

    public int sendMessage(DoipMessage msg, String host, int port) throws Exception
    {
        /**
         * @Description  配置相应的参数，提供连接到远端的方法
         **/
        long startTime = System.currentTimeMillis();
        EventLoopGroup group = new NioEventLoopGroup();//I/O线程池
        int maxFrame = 5 * 1024 * 1024;
        try{
            Bootstrap bs = new Bootstrap();//客户端辅助启动类
            bs.group(group)
                    .channel(NioSocketChannel.class)//实例化一个Channel
                    .option(ChannelOption.TCP_NODELAY, true)
                    .remoteAddress(new InetSocketAddress(host,port))
                    .handler(new ChannelInitializer<SocketChannel>()//进行通道初始化配置
                    {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception
                        {
                            socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(maxFrame,
                                            20, 4, 0, 0))
                                    .addLast(new MessageEnvelopeCodec())
                                    .addLast(new MessageEnvelopeAggregator(maxFrame - MessageEnvelope.ENVELOPE_LENGTH))
                                    .addLast(new MockClientHandler());//添加我们自定义的Handler
//
                        }
                    });

            //异步连接到远程节点；等待连接完成
            ChannelFuture future=bs.connect().sync();
//            logger.info("连接到远端Switch listener");
            long endTime = System.currentTimeMillis();
            long duration = endTime-startTime;
            System.out.println("Mock client connect time :" + duration + "ms");
            //发送DOIP消息到服务器端
            //future.channel().writeAndFlush(Unpooled.copiedBuffer(doipmessage.toString(), CharsetUtil.UTF_8));
            long threadStartTime = System.currentTimeMillis();
            ChannelFuture cf=future.channel().writeAndFlush(msg);
            cf.addListener((ChannelFutureListener) future1 -> {
                //写操作完成，并没有错误发生
                if (future1.isSuccess()){
                    System.out.println("successful");
                }else{
                    //记录错误
                    System.out.println("error");
                    future1.cause().printStackTrace();
                }
            });
//            logger.info("MockUserClient has sended.");
            //阻塞操作，closeFuture()开启了一个channel的监听器（这期间channel在进行各项工作），直到链路断开
            future.channel().closeFuture().sync();
            long threadEndTime = System.currentTimeMillis();
            threadDuration = threadEndTime - threadStartTime;
//            logger.info("request cost time: "+threadDuration+"ms");
        } finally{
            group.shutdownGracefully().sync();
        }
        return (int) threadDuration;
    }

}
