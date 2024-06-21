package org.bdware.sw;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.endpoint.server.DoipListenerConfig;
import org.bdware.sw.builder.TCPListenerBuilder;
import org.bdware.sw.director.DirectorForListener;
import org.bdware.sw.listener.DataSwitchTCPListener;
import org.bdware.sw.nodemanager.SwitchNodeManager;
import org.bdware.sw.statistics.Statistics;

import java.util.List;


public class DataSwitch {
    static Logger logger = LogManager.getLogger(DataSwitch.class);
    public static String switchId;
    private String host;
    private int port;
    public DataSwitchTCPListener nettyDataSwitchListener;
    public DirectorForListener directorForListener;

    public void setSwitchId(String switchId) {
        this.switchId = switchId;
    }

    public void setHost(String host){
        this.host = host;
    }

    public void DataSwitch(){

    }

    public void setPort(int port){
         this.port = port;
    }


    public void setNettyDataSwitchListener(SwitchManagementIntf switchManagementIntf,  EndpointConfig config, List<IrpDetector.StaticRouteEntry> entryList, Runnable afterStartCallback) {
        try {
            directorForListener = new DirectorForListener(new TCPListenerBuilder());
            this.nettyDataSwitchListener = directorForListener.constructTCPListener(this.host, this.port, switchManagementIntf, new DoipListenerConfig("localhost", "2.1"), config, entryList, afterStartCallback);
        }catch (Exception e){
            logger.error("Error while creating listener", e);
        }

    }

//    public DataSwitch(String host, int port, SwitchManagementIntf switchManagementIntf, EndpointConfig config, String switchID, List<IrpDetector.StaticRouteEntry> entryList, Runnable afterStartCallback) throws Exception {
//        this.host = host;
//        this.port = port;
//        this.switchId = switchID;
//        this.nettyDataSwitchListener = new DataSwitchTCPListener(host, port, switchManagementIntf, new DoipListenerConfig("localhost", "2.1"), config, entryList, afterStartCallback);
////        this.nettyDataSwitchListener = new DataSwitchUDPListener(host, port, switchID, new DoipListenerConfig("localhost", "2.1"), config, afterStartCallback);
//    }

    public Statistics getStatistics() {
        return nettyDataSwitchListener.getStatistics();
    }


}
