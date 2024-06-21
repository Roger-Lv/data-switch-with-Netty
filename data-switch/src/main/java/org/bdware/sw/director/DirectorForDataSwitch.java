package org.bdware.sw.director;

import org.bdware.doip.audit.EndpointConfig;
import org.bdware.sw.DataSwitch;
import org.bdware.sw.IrpDetector;
import org.bdware.sw.SwitchManagementIntf;
import org.bdware.sw.builder.DataSwitchBuilder;

import java.util.List;

public class DirectorForDataSwitch {
    private DataSwitchBuilder builder;
    private DirectorForListener directorForListener;

    public DirectorForDataSwitch(DataSwitchBuilder builder) {
        this.builder = builder;
    }

    public DataSwitch constructDataSwitch(String host, int port, SwitchManagementIntf switchManagementIntf, EndpointConfig config, String switchID, List<IrpDetector.StaticRouteEntry> entryList, Runnable afterStartCallback) {
        builder.buildHost(host);
        builder.buildPort(port);
        builder.buildSwitchId(switchID);
        builder.buildNettyDataSwitchListener(host, port, switchManagementIntf, config, entryList, afterStartCallback);
        return builder.getDataSwitch();
    }
}
