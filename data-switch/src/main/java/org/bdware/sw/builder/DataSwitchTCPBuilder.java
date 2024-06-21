package org.bdware.sw.builder;

import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.endpoint.server.DoipListenerConfig;
import org.bdware.doip.endpoint.server.NettyTCPDoipListener;
import org.bdware.sw.IrpDetector;
import org.bdware.sw.SwitchManagementIntf;
import org.bdware.sw.listener.DataSwitchTCPListener;
import org.bdware.sw.listener.NettyDataSwitchListener;

import java.util.List;

public class DataSwitchTCPBuilder extends DataSwitchBuilder
{
    @Override
    public void buildSwitchId(String switchId) {
        dataSwitch.setSwitchId(switchId);
    }

    @Override
    public void buildHost(String host) {
        dataSwitch.setHost(host);
    }

    @Override
    public void buildPort(int port) {
        dataSwitch.setPort(port);
    }

    @Override
    public void buildNettyDataSwitchListener(String host, int port, SwitchManagementIntf switchManagementIntf,
                                              EndpointConfig config,
                                             List<IrpDetector.StaticRouteEntry> entryList, Runnable afterStartCallback) {
        dataSwitch.setNettyDataSwitchListener(switchManagementIntf,  config, entryList, afterStartCallback);
    }

}
