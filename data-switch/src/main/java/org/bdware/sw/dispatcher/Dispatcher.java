package org.bdware.sw.dispatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.sw.nodemanager.AddressOfSwitch;
import org.bdware.sw.nodemanager.DigitalSpaceNodeManager;
import org.bdware.sw.nodemanager.SwitchNodeManager;


/**
 * @Description 解析消息，到底是通过哪个client发给谁
 **/
public class Dispatcher {
    static Logger LOGGER = LogManager.getLogger(Dispatcher.class);
    // SwitchNodeManager：包含自身已知的其他交换机节点的switchname-ip:port-List[DigitalSpace-List[Repo-List[data]]]信息
    public static SwitchNodeManager switchNodeManager;
    // DigitalSpaceNodeManager: 包含自身拥有的管理List[DigitalSapce-List[repo-ip:port]]的管理信息
    public DigitalSpaceNodeManager digitalSpaceNodeManager;
    public EndpointConfig endpointConfig;
    public String host;
    public int port;

    public Dispatcher(String host, int port, EndpointConfig endpointConfig) {
        this.switchNodeManager = new SwitchNodeManager();
        this.digitalSpaceNodeManager = new DigitalSpaceNodeManager(endpointConfig);
        this.endpointConfig = endpointConfig;
        this.host = host;
        this.port = port;
    }

    // TODO
    //判断是不是请求。
    // 1 如果是请求，就看是不是这个空间可以处理的，
    //    1.1 如果是这个空间可以处理的，就使用DoaClusterClient发送，并将结果返回。
    //    1.2 如果不是这个空间可以处理的，那调用定位系统的接口，使用DoaClusterClient获取定位信息；而后使用DoaClusterClient/Client向目标路由器发请求，最后将结果返回。
    // 2.如果是响应（由于现在是基于TCP的连接，没有这种情况，所有响应均在1.1 1.2的回调里处理)。
    public boolean isRequest(DoipMessage msg) {
        return msg.isRequest();
    }

    public DispatchAnswer dispatch(DoipMessage msg) throws Exception {
        DispatchAnswer dispatchAnswer = new DispatchAnswer();
        try {
            Object objectInDigitalNodeManager = digitalSpaceNodeManager.ifInCache(msg);
            Object objectInSwitchNodeManager;
            if (objectInDigitalNodeManager != null) {
                dispatchAnswer.situation = 0;
                if (objectInDigitalNodeManager instanceof DoipMessage) {
                    dispatchAnswer.doipMessage = (DoipMessage) objectInDigitalNodeManager;
                    dispatchAnswer.addressOfSwitch = null;
                    dispatchAnswer.endpointConfig = null;
                } else if (objectInDigitalNodeManager instanceof EndpointConfig) {
                    dispatchAnswer.addressOfSwitch = null;
                    dispatchAnswer.doipMessage = null;
                    dispatchAnswer.endpointConfig = (EndpointConfig) objectInDigitalNodeManager;
                }
                // 将请求移交给ClusterClientForDigitalSpace
                return dispatchAnswer;
            } else {
                LOGGER.debug("不属于该交换机");
                objectInSwitchNodeManager = switchNodeManager.ifInCache(msg);
            }
            if (objectInDigitalNodeManager == null && objectInSwitchNodeManager != null) {
                //Caffiene缓存有信息
                dispatchAnswer.situation = 1;
                if (objectInSwitchNodeManager instanceof DoipMessage) {
                    //返回Doip的请求
                    dispatchAnswer.endpointConfig = null;
                    dispatchAnswer.addressOfSwitch = null;
                    dispatchAnswer.doipMessage = (DoipMessage) objectInSwitchNodeManager;
                }
                //将请求移交给ClientForSwitch
                else if (objectInSwitchNodeManager instanceof AddressOfSwitch) {
                    dispatchAnswer.endpointConfig = null;
                    dispatchAnswer.doipMessage = null;
                    dispatchAnswer.addressOfSwitch = (AddressOfSwitch) objectInSwitchNodeManager;
                }
                return dispatchAnswer;
            } else {
                //如果都不在缓存中，则交给ClusterClientForLocationSystem查询 getKV
                dispatchAnswer.situation = 2;
                dispatchAnswer.endpointConfig = null;
                return dispatchAnswer;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}