package org.bdware.sw.handler;

import com.google.gson.JsonObject;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.bdware.irp.stateinfo.StateInfoBase;
import org.bdware.sw.IrpDetector;
import org.bdware.sw.SM3Tool;
import org.bdware.sw.client.ClientForDigitalSpace;
import org.bdware.sw.client.ClientForLocationSystem;
import org.bdware.sw.client.clusterclient.ClusterClientForSwitch;
import org.bdware.sw.dispatcher.DispatchAnswer;
import org.bdware.sw.dispatcher.Dispatcher;
import org.bdware.sw.monitor.MetricsForGrafana;
import org.bdware.sw.nodemanager.AddressOfSwitch;

public class SwitchFunctions {
    static Logger logger = LogManager.getLogger(SwitchFunctions.class);
    //metricsForGrafana
    private MetricsForGrafana metrics;

    public SwitchFunctions(MetricsForGrafana metrics) {
        this.metrics = metrics;
    }

    public void retrieveByClusterClientForDigitalSpace(ChannelHandlerContext ctx, DoipMessage message, DispatchAnswer dispatchAnswer,
                                                       ClientForDigitalSpace digitalSpaceClusterClient) {

        DoipMessage answer = dispatchAnswer.doipMessage;
        if (answer != null) {
//            answer.requestID = message.requestID;
            message.body = answer.body;
            message.header = answer.header;
            ChannelFuture future = ctx.writeAndFlush(message);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // 写入成功
                        logger.info("成功返回至客户端");
                    } else {
                        // 写入失败
                        logger.info("Write failed");
                        future.cause().printStackTrace();
                    }
                }
            });
            logger.info("return DoipMessage by Caffiene in DigitalSpaceNodeManager:" + message.body.getDataAsJsonString());
        } else if (dispatchAnswer.endpointConfig != null) {
            EndpointConfig endpointConfig = dispatchAnswer.endpointConfig;
            //将请求移交给ClusterClientForDigitalSpace
            digitalSpaceClusterClient.sendMessage(message, new DoipMessageCallback() {
                @Override
                public void onResult(DoipMessage msg) {
                    ChannelFuture future = ctx.writeAndFlush(msg);
                    future.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) {
                            if (future.isSuccess()) {
                                // 写入成功
                            } else {
                                // 写入失败
                                logger.error("Write failed");
                                future.cause().printStackTrace();
                            }
                        }
                    });
//                    if (message.header.parameters.operation.equals(BasicOperations.Retrieve.getName())) {
//                        synchronized (DigitalSpaceNodeManager.cacheForDoipMessage) {
//                            DigitalSpaceNodeManager.cacheForDoipMessage.put(msg.header.parameters.id, msg);
//                        }
//                    }

                }
            });


        } else {
            logger.info("case 0,but is the situation is wrong, not in caffiene and the endpointConfig is null, can't deal with it");
        }

    }

    public void retrieveByClientForSwitch(ChannelHandlerContext ctx, DoipMessage message, DispatchAnswer dispatchAnswer,
                                          ClusterClientForSwitch clientForSwitch, Dispatcher dispatcher) throws Exception {

        DoipMessage answer = dispatchAnswer.doipMessage;
        if (answer != null) {
//            answer.requestID = message.requestID;
            message.body = answer.body;
            message.header = answer.header;
            ChannelFuture future = ctx.writeAndFlush(message);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // 写入成功
                        logger.info("成功返回至客户端");
                    } else {
                        // 写入失败
                        logger.info("Write failed");
                        future.cause().printStackTrace();
                    }
                }
            });
            logger.info("return DoipMessage by Caffiene in SwitchNodeManager:" + message.body.getDataAsJsonString());
        } else if (dispatchAnswer.addressOfSwitch != null) {
            //将请求移交给ClientForSwitch
            AddressOfSwitch addressOfSwitch = dispatchAnswer.addressOfSwitch;
            String host = addressOfSwitch.swtichIp;
            int port = addressOfSwitch.port;
            clientForSwitch.sendMessage(message, host, port, new DoipMessageCallback() {
                @Override
                public void onResult(DoipMessage msg) {
                    ChannelFuture future = ctx.writeAndFlush(msg);
                    future.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) {
                            if (future.isSuccess()) {
                                // 写入成功
                                //logger.info("成功返回至客户端");
                            } else {
                                // 写入失败
                                logger.info("Write failed");
                                future.cause().printStackTrace();
                            }
                        }
                    });
//                    if (message.header.parameters.operation.equals(BasicOperations.Retrieve.getName())) {
//                        SwitchNodeManager.cacheForDoipMessage.put(msg.header.parameters.id,msg);
//                        synchronized (SwitchNodeManager.cacheLock.get(msg.header.parameters.id)){
//                            SwitchNodeManager.cacheLock.get(msg.header.parameters.id).notifyAll();
//                            logger.info("unlock the lock");
//                            SwitchNodeManager.cacheLock.remove(msg.header.parameters.id);
//                        }
//                    }
                }
            });

        } else {
            logger.info("case 1,but is the situation is wrong, not in caffiene and the address is null, can't deal with it");
        }

    }

    public void retrieveByGetKV(ChannelHandlerContext ctx, DoipMessage message,
                                IrpDetector irpDetector, ClientForLocationSystem locationSystemClusterClient,
                                ClusterClientForSwitch clientForSwitch, Dispatcher dispatcher) throws Exception {
        //get mockKV
        String key = message.header.parameters.id;
        String sm3Key = SM3Tool.toSM3(key);
        JsonObject result1 = locationSystemClusterClient.getData(sm3Key);
//        logger.info(result1.toString());
        // final String[] URI = {""}; //占位
        //@LZRJ 这里需要判断下没有result的时候要特定地处理一下。
        //如果为空
        if (result1.get("result") == null) {
            irpDetector.getResult(new IrpDetector.DetectCallback() {
                @Override
                public void onResult(IrpDetector.StaticRouteEntry entry, StateInfoBase resolveResult) {
                    if (resolveResult == null) {
                        logger.error("The id is not in Location System and any routeEntry!");
                        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
                        builder.createRequest("ToClient", BasicOperations.Unknown.getName());
                        builder.setBody("The id is not in Location System and any routeEntry!".getBytes());
                        DoipMessage answer = builder.create();
                        message.header = answer.header;
                        message.body = answer.body;
                        ctx.writeAndFlush(message);
                        return;
                    } else {
                        //解析到 存入到MockKV中
                        logger.info("The id:" + key + " is not in Location System, found in routeEntry:" +
                                " switchURI:" + entry.switchURI + " switchID:" + entry.switchID);
                        try {
                            processURIParts(entry.switchURI.split(":"), clientForSwitch, dispatcher, key, message, ctx);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        locationSystemClusterClient.updateData(sm3Key, entry.switchID);
                    }
                }
            }, key);
        } else {
            processURIParts(result1.get("result").toString().split(":"), clientForSwitch, dispatcher, key, message, ctx);
        }


    }

    private void processURIParts(String[] parts, ClusterClientForSwitch clientForSwitch, Dispatcher dispatcher, String key, DoipMessage message, ChannelHandlerContext ctx) throws Exception {
        parts[0] = parts[0].replace("\"", "");
        parts[1] = parts[1].replace("\"", "");
        //将请求移交给ClusterClientForDigitalSpace(如果Host和对应的port就是switch的ip port)
        if (parts[0].equals(dispatcher.host) && Integer.parseInt(parts[1]) == dispatcher.port) //与初始化的host和Ip相同
        { //与后续添加的endpointConfig相同
//            dispatcher.digitalSpaceNodeManager.addCache(key,endpointConfig); //添加到缓存 target->endpointConfig中
            logger.info("case 2,but is itself.The situation is wrong, can't deal with it");
            throw new IllegalStateException("loop detected from SwitchFunctions.processURIParts!");
            //这里sendMessage是获取到endpoint对应的hash
//            digitalSpaceClusterClient.sendMessage(msg, endpointConfig,new DoipMessageCallback() {
//                @Override
//                public void onResult(DoipMessage msg) {
//                    logger.info("digitalSpaceClusterClient已经接收到回调：" + msg.body.getDataAsJsonString());
//                    ctx.writeAndFlush(msg);
//                }
//            });
        } else {
            //将请求移交给ClientForSwitch
            dispatcher.switchNodeManager.addAddressCache(key, new AddressOfSwitch("switchId", parts[0], Integer.parseInt(parts[1])));
            AddressOfSwitch threeTuple = dispatcher.switchNodeManager.getAddressCache(key);
            String host = threeTuple.swtichIp;
            int port = threeTuple.port;
            clientForSwitch.sendMessage(message, host, port, new DoipMessageCallback() {
                @Override
                public void onResult(DoipMessage msg) {
                    ChannelFuture future = ctx.writeAndFlush(msg);
                    future.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) {
                            if (future.isSuccess()) {
                                // 写入成功
                                // logger.info("成功返回至客户端");
                            } else {
                                // 写入失败
                                logger.info("Write failed");
                                future.cause().printStackTrace();
                            }
                        }
                    });
//                    if (message.header.parameters.operation.equals(BasicOperations.Retrieve.getName())&&msg.body.getDataAsJsonString().length()>0) {
//                        SwitchNodeManager.cacheForDoipMessage.put(key, msg);
//                        synchronized (SwitchNodeManager.cacheLock.get(key)) {
//                            SwitchNodeManager.cacheLock.get(key).notifyAll();
//                            logger.info("unlock the lock");
//                            SwitchNodeManager.cacheLock.remove(key);
//                        }
//                    }
                }
            });
        }
    }

//    public void returnDoipMessageByCaffiene(ChannelHandlerContext ctx, DoipMessage request,DoipMessage answer)throws Exception{
//
//        request.body=answer.body;
//        request.header.parameters.attributes=answer.header.parameters.attributes;
//        ChannelFuture future =ctx.writeAndFlush(request);
//        future.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) {
//                if (future.isSuccess()) {
//                    // 写入成功
//                    logger.info("成功返回至客户端");
//                } else {
//                    // 写入失败
//                    logger.info("Write failed");
//                    future.cause().printStackTrace();
//                }
//            }
//        });
//        logger.info("returnDoipMessageByCaffiene:"+answer.body.getDataAsJsonString());
//        String key = request.header.parameters.id;
//        AddressOfSwitch addressOfSwitch = dispatcher.switchNodeManager.ifContainsAddress(request.header.parameters.id);
//        if (addressOfSwitch!=null){
//            //update Caffiene by get the data now
//            String host = addressOfSwitch.swtichIp;
//            int port = addressOfSwitch.port;
//            clientForSwitch.sendMessage(request, host, port, new DoipMessageCallback() {
//                @Override
//                public void onResult(DoipMessage msg) {
//                    SwitchNodeManager.cacheForDoipMessage.put(key,msg);
//                    logger.info("ClientForSwitch更新Caffine完成,key为: " +key+",value为: "+msg.body.getDataAsJsonString());
//                }
//            });
//        }else{
//            logger.info("get mockKV to update Caffiene.");
//            String sm3Key = SM3Tool.toSM3(key);
//            JsonObject result1 = locationSystemClusterClient.executeContract("MockKV", "get", "{\"key\":\"" + sm3Key + "\"}");
//            logger.info(result1.toString());
//            if (result1.get("result")==null){
//                DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
//                builder.createRequest("ToClient", BasicOperations.Unknown.getName());
//                builder.setBody("the data maybe outOfTime".getBytes());
//                ctx.writeAndFlush(builder.create());
//                return;
//            }
//            String parts[] = result1.get("result").toString().split(":");
//            parts[0] = parts[0].replace("\"", "");
//            parts[1] = parts[1].replace("\"", "");
//            dispatcher.switchNodeManager.addAddressCache(key, new AddressOfSwitch("switchId", parts[0], Integer.parseInt(parts[1])));
//            AddressOfSwitch threeTuple = dispatcher.switchNodeManager.getAddressCache(key);
//            String host = threeTuple.swtichIp;
//            int port = threeTuple.port;
//            clientForSwitch.sendMessage(request, host, port, new DoipMessageCallback() {
//                @Override
//                public void onResult(DoipMessage msg) {
//                    SwitchNodeManager.cacheForDoipMessage.put(key,msg);
//                    logger.info("ClientForSwitch更新Caffine完成,key为: " +key+",value为: "+msg.body.getDataAsJsonString());
//                }
//            });
//
//        }


//    }

    public void handleWrongMsg(DoipMessage doipMessage, ChannelHandlerContext ctx) {
        logger.error("wrong doipMessage!");
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("ToClient", BasicOperations.Unknown.getName());
        builder.setBody("wrong doipMessage!".getBytes());
        DoipMessage answer = builder.create();
        doipMessage.header = answer.header;
        doipMessage.body = answer.body;
        ctx.writeAndFlush(doipMessage);
    }
}
