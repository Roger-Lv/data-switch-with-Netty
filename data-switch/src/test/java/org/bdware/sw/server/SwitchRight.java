package org.bdware.sw.server;

import com.google.gson.JsonObject;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.sw.DataSwitch;
import org.bdware.sw.IrpDetector;
import org.bdware.sw.SwitchManagementIntf;
import org.bdware.sw.builder.DataSwitchBuilder;
import org.bdware.sw.builder.DataSwitchTCPBuilder;
import org.bdware.sw.director.DirectorForDataSwitch;
import org.bdware.sw.nodemanager.AddressOfSwitch;

import java.util.ArrayList;
import java.util.List;


public class SwitchRight {
    public static void runRight() throws Exception {
        String rightSwitchHost = "127.0.0.1";
        int rightSwitchPort = 2043;
        String rightSwitchId = "rightSwtich999999";
        EndpointConfig endpointConfig = new EndpointConfig();
        //DOI
        endpointConfig.routerURI = "tcp://8.130.115.76:21041";

        //IDS
//        endpointConfig.routerURI = "tcp://8.130.136.43:21042";
        //github entry
        IrpDetector.StaticRouteEntry entry = new IrpDetector.StaticRouteEntry();
        entry.switchURI = "8.130.136.43:21060";
        entry.resolverURI = "tcp://8.130.136.43:21041";
        entry.spaceName = "github";
        List<IrpDetector.StaticRouteEntry> entryList = new ArrayList<>();
        entryList.add(entry);
        DataSwitchBuilder builder = new DataSwitchTCPBuilder();
        DirectorForDataSwitch director = new DirectorForDataSwitch(builder);
        DataSwitch rightSwitch = director.constructDataSwitch(rightSwitchHost, rightSwitchPort, new SwitchManagementIntf() {
            @Override
            public String updateConfig(JsonObject updateItem) {
                return "not supported";
            }
        }, endpointConfig, rightSwitchId, entryList, () -> {
            //这里暂时不publish
        });

//        rightSwitch.nettyDataSwitchListener.dispatcher.switchNodeManager.addCache("https://github.com/maxfischer2781/bootpeg",new AddressOfSwitch("left","127.0.0.1",2042));
        rightSwitch.nettyDataSwitchListener.dispatcher.switchNodeManager.addAddressCache("https://github.com/johnnlp/saved", new AddressOfSwitch("left", "127.0.0.1", 2042));

        rightSwitch.nettyDataSwitchListener.start();
    }

    public static void main(String[] args) throws Exception {
        runRight();
    }
}
