package org.bdware.sw.server.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.audit.client.AuditIrpClient;
import org.bdware.irp.stateinfo.StateInfoBase;
import org.bdware.sw.client.FixedAuditIrpClient;
import org.bdware.sw.nodemanager.DigitalSpaceNodeManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CacheDSNMTest {

//    public static void main(String[] args) throws Exception {
//        EndpointConfig endpointConfig = new EndpointConfig();
//        endpointConfig.routerURI="tcp://127.0.0.1:18041";
//        String filePath = "data-switch/src/test/java/org/bdware/sw/server/puregithubid.json";
//        DigitalSpaceNodeManager digitalSpaceNodeManager=new DigitalSpaceNodeManager(endpointConfig);
//        List<String> idList = getIdList(filePath);
//        idList = idList.subList(0,1);
//        float size = idList.size();
//        System.out.println("size："+size);
//        //准备工作
//        for (String id:idList){
//            digitalSpaceNodeManager.endpointCache.put(id,endpointConfig);
//        }
//        digitalSpaceNodeManager.configAuditIrpClientConcurrentHashMap.put(endpointConfig,new FixedAuditIrpClient(endpointConfig));
//        //AuditIrpClient 命中
//        double startTimeOfAuditIrpClient = System.currentTimeMillis();
//        int hitNum = 0;
//        for (String id :idList){
//            for (EndpointConfig endpointConfig1:digitalSpaceNodeManager.configAuditIrpClientConcurrentHashMap.keySet()){
//                StateInfoBase result = digitalSpaceNodeManager.configAuditIrpClientConcurrentHashMap.get(endpointConfig1).resolve(id);
//                if (result != null && result.handleValues != null) {
//                    hitNum++;
//                    continue;
//                }
//            }
//        }
//        double endTimeOfAuditIrpClient = System.currentTimeMillis();
//        double costTimeOfAuditIrpClient = endTimeOfAuditIrpClient-startTimeOfAuditIrpClient;
//        double avgCostTimeOfAuditIrpClient = costTimeOfAuditIrpClient/size;
//        System.out.println("hitNum/size："+hitNum+"/"+size);
//        System.out.println("吞吐量："+hitNum/costTimeOfAuditIrpClient*1000+"/s");
//        System.out.println("cost Time of AuditIrpClient: "+costTimeOfAuditIrpClient+"ms");
//        System.out.println("avg cost Time of AuditIrpClient: "+avgCostTimeOfAuditIrpClient+"ms");
//        //cache 命中
//        double startTimeOfCache = System.currentTimeMillis();
//        for (String id:idList){
//            digitalSpaceNodeManager.endpointCache.get(id);
//        }
//        double endTimeOfCache = System.currentTimeMillis();
//        double costTimeOfCache = endTimeOfCache-startTimeOfCache;
//        double avgCostTimeOfCache = costTimeOfCache/size;
//        System.out.println("cost Time of cache: "+costTimeOfCache+"ms");
//        System.out.println("avg cost Time of cache: "+avgCostTimeOfCache+"ms");
//
//
//
//    }

    public static List<String> getIdList(String filePath){
        List<String> idList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode node = mapper.readTree(line);
                String id = node.get("id").textValue();
                if (id.startsWith("https://github.com/"))
                idList.add(id);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("size of list:"+idList.size());
        return idList;
    }

}
