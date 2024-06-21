package org.bdware.sw.client;

import org.bdware.client.SmartContractHttpClient;
import org.bdware.sw.IrpDetector;
import org.zz.gmhelper.SM2KeyPair;

import java.util.List;

public class ClientForLocationSystem_BACKUP extends SmartContractHttpClient {

    public IrpDetector irpDetector;
    public ClientForLocationSystem_BACKUP(SM2KeyPair pair, String ip, int port) {
        super(pair, ip, port);
    }
    public ClientForLocationSystem_BACKUP(SM2KeyPair pair, String ip, int port, String method) {
        super(pair, ip, port, method);
    }
    public ClientForLocationSystem_BACKUP(SM2KeyPair pair, String ip, int port, String method, List<IrpDetector.StaticRouteEntry> entryList) {
        super(pair, ip, port, method);
        this.irpDetector = new IrpDetector(entryList);
    }
}


