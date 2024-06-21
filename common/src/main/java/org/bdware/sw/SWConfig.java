package org.bdware.sw;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bdware.sc.boundry.utils.RocksDBUtil;
import org.bdware.sc.util.JsonUtil;

import java.util.List;

public class SWConfig {
    public boolean overrideLocalConfig;
    public int port;
    public String ip;
    public String name;
    public String id;
    public String gatewayIRPURL;
    public String gatewayId;
    public String gatewayDOIPURL;
    public String routerIRPURL;
    public String routerId;
    public String managerPublicKey;
    public List<PublisherInfo> publishIdTopics;
    public transient SwitchManagementIntf switchManagent;
    public String pragmaticId;
    public static SWConfig globalConfig;
    public List<IrpDetector.StaticRouteEntry> staticRouteEntry;

    public int prometheusPort;
    public String logstashURL;


    public void save(RocksDBUtil storage) {
        storage.put("__CONFIG__", JsonUtil.toJson(this));
    }

    public JsonObject listConfig() {
        JsonObject jo = new JsonObject();
        jo.addProperty("id", id);
        jo.addProperty("ip", ip);
        jo.addProperty("port", port);
        jo.addProperty("name", name);
        jo.addProperty("gatewayId", gatewayId);
        jo.addProperty("gatewayIRPURL", gatewayIRPURL);
        jo.addProperty("gatewayDOIPURL", gatewayDOIPURL);
        jo.addProperty("routerIRPURL", routerIRPURL);
        jo.addProperty("routerId", routerId);
        jo.addProperty("managerPublicKey", managerPublicKey);
        jo.addProperty("logstashURL", logstashURL);
        return jo;
    }

    public static JsonObject mergeStoredConfig(JsonObject startConfig, JsonObject localConfig) {
        if (startConfig.has("overrideLocalConfig") && startConfig.get("overrideLocalConfig").getAsBoolean()) {
            for (String key : startConfig.keySet()) localConfig.add(key, startConfig.get(key));
            return localConfig;
        } else {
            for (String key : localConfig.keySet()) startConfig.add(key, localConfig.get(key));
            return startConfig;
        }
    }

    public static JsonObject fromLocal(RocksDBUtil storage) {
        String persistntConfig = storage.get("__CONFIG__");
        if (persistntConfig != null && persistntConfig.length() > 0)
            return JsonParser.parseString(persistntConfig).getAsJsonObject();
        else return new JsonObject();
    }
}