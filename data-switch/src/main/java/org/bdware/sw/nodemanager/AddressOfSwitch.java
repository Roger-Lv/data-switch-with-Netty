package org.bdware.sw.nodemanager;

public class AddressOfSwitch {
    //swtich-ip-port-repoName
    public final String switchId;
    public final String swtichIp;
    public final int port;

    public AddressOfSwitch(String switchId, String swtichIp, int port) {
        //left
        this.switchId = switchId;
        //127.0.0.1
        this.swtichIp = swtichIp;
        //2042
        this.port = port;


    }
    // Add getters if needed

    @Override
    public String toString() {
        return "(" + switchId + ", " + swtichIp + ", " + port + ")";
    }
}
