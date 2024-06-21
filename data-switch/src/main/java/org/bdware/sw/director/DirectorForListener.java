package org.bdware.sw.director;

import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.endpoint.server.DoipListenerConfig;
import org.bdware.sw.IrpDetector;
import org.bdware.sw.SwitchManagementIntf;
import org.bdware.sw.builder.ListenerBuilder;
import org.bdware.sw.builder.TCPListenerBuilder;
import org.bdware.sw.listener.DataSwitchTCPListener;
import org.bdware.sw.listener.NettyDataSwitchListener;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DirectorForListener {
    private TCPListenerBuilder builder;

    public DirectorForListener(TCPListenerBuilder builder){
        this.builder = builder;
    }

    public DataSwitchTCPListener constructTCPListener(String host, int port, SwitchManagementIntf switchManagementIntf,
                                                      DoipListenerConfig listenerConfig, EndpointConfig config,
                                                      List<IrpDetector.StaticRouteEntry> entryList, Runnable afterStartCallback) throws URISyntaxException, InterruptedException, ExecutionException, ExecutionException {
        return builder.getDataSwitchTCPListener(host, port, switchManagementIntf, listenerConfig, config, entryList, afterStartCallback);

    }


}
