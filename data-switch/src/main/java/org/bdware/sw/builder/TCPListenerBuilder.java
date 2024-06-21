package org.bdware.sw.builder;

import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.endpoint.server.DoipListenerConfig;
import org.bdware.doip.endpoint.server.NettyTCPDoipListener;
import org.bdware.sw.IrpDetector;
import org.bdware.sw.SwitchManagementIntf;
import org.bdware.sw.listener.DataSwitchTCPListener;

import java.net.URISyntaxException;
import java.util.List;

public class TCPListenerBuilder extends ListenerBuilder {

    public DataSwitchTCPListener getDataSwitchTCPListener(String host, int port, SwitchManagementIntf switchManagementIntf,
                                                          DoipListenerConfig listenerConfig, EndpointConfig config,
                                                          List<IrpDetector.StaticRouteEntry> entryList, Runnable afterStartCallback) throws URISyntaxException, InterruptedException {
        return new DataSwitchTCPListener(host, port, switchManagementIntf, listenerConfig, config, entryList, afterStartCallback);
    }

}

