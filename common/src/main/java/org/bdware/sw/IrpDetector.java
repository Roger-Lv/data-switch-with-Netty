package org.bdware.sw;

import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.audit.client.AuditIrpClient;
import org.bdware.irp.stateinfo.StateInfoBase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class IrpDetector {
    ExecutorService es = Executors.newFixedThreadPool(8);
    Map<StaticRouteEntry, AuditIrpClient> connections;

    public static class StaticRouteEntry {
        public String spaceName;
        public String resolverURI;
        public String switchURI;
        public String switchID;
    }

    public interface DetectCallback {
        void onResult(StaticRouteEntry entry, StateInfoBase resolveResult);
    }

    public IrpDetector(List<StaticRouteEntry> entryList) {
        connections = new HashMap<>();
        for (StaticRouteEntry entry : entryList) {
            EndpointConfig config = new EndpointConfig();
            config.routerURI = entry.resolverURI;
            connections.put(entry, new AuditIrpClient(config));
        }
    }


    public void getResult(DetectCallback callback, String id) {
        AtomicInteger failedCount = new AtomicInteger(0);
        for (StaticRouteEntry entry : connections.keySet()) {
            AuditIrpClient client = connections.get(entry);
            es.execute(() -> {
                try {
                    StateInfoBase result = client.resolve(id);
                    if (result != null) {
                        if (result.handleValues != null) {
                            if (result.handleValues.has("name") && result.handleValues.get("name").getAsString().equals("NotFound")) {
                            } else {
                                callback.onResult(entry, result);
                                return;
                            }
                        }
                    }
                    {
                        int fc = failedCount.incrementAndGet();
                        if (fc == connections.keySet().size()) {
                            callback.onResult(null, null);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
