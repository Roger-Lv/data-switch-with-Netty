package org.bdware.sw.builder;

import org.bdware.doip.audit.EndpointConfig;
import org.bdware.sw.DataSwitch;
import org.bdware.sw.IrpDetector;
import org.bdware.sw.SwitchManagementIntf;

import java.util.List;

public abstract class DataSwitchBuilder {
    protected DataSwitch dataSwitch = new DataSwitch();

    public abstract void buildSwitchId(String switchId);

    public abstract void buildHost(String host);

    public abstract void buildPort(int port);

    public abstract void buildNettyDataSwitchListener(String host, int port, SwitchManagementIntf switchManagementIntf,
                                                      EndpointConfig config,
                                                      List<IrpDetector.StaticRouteEntry> entryList, Runnable afterStartCallback);

    public DataSwitch getDataSwitch(){
        return dataSwitch;
    }
}


