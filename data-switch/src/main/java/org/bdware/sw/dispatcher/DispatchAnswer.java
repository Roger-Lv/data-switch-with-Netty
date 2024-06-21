package org.bdware.sw.dispatcher;

import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.sw.nodemanager.AddressOfSwitch;

public class DispatchAnswer {
    public int situation;
    public EndpointConfig endpointConfig;
    public AddressOfSwitch addressOfSwitch;
    public DoipMessage doipMessage;
}
