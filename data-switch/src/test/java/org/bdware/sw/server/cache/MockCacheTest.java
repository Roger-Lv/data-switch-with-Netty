package org.bdware.sw.server.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bdware.sw.nodemanager.AddressOfSwitch;
import org.bdware.sw.nodemanager.cache.LFU.LFUCache;
import org.bdware.sw.nodemanager.cache.LRU.MyLRUCache;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockCacheTest {


    public static void main(String[] args) {
        String filePath = "data-switch/src/test/java/org/bdware/sw/server/peretoWindowRandomIdList.json";
//        MyLRUCache<String,AddressOfSwitch> cache = new MyLRUCache<String,AddressOfSwitch>(5000);
//        LRUCache<String,AddressOfSwitch>cache = new LRUCache<>(450000);
        LFUCache cache = new LFUCache(450000);
//        SLRUCache cache = new SLRUCache(450000);
//        LRU2 cache = new LRU2<>(320000);
//        Cache<String,AddressOfSwitch> cache = Caffeine.newBuilder().maximumSize(5000).build();
        List<String>idList = getIdList(filePath);

        float size = idList.size();
        System.out.println("size of list:"+idList.size());
        float numOfHit = 0;
        long startTime = System.currentTimeMillis();
        for (String id:idList){
            AddressOfSwitch addressOfSwitch= (AddressOfSwitch) cache.get(id);
//            AddressOfSwitch addressOfSwitch=  cache.getIfPresent(id);//caffeine的
            if (addressOfSwitch!=null){
                numOfHit++;
                System.out.println("hit");
            }else{
                System.out.println("not hit");
                cache.put(id,new AddressOfSwitch("1","127.0.0.1",2041));
            }
        }
        long endTime=System.currentTimeMillis();
        long duration = endTime-startTime;
        float hitRatio=numOfHit/size;
        System.out.println("cost time: "+duration+"ms");
        System.out.println("size of list:"+idList.size());
        System.out.println("hit ratio:"+hitRatio);
    }
    public static List<String> getIdList(String filePath){
        List<String> idList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                line=line.replace("\t","");
                JsonNode node = mapper.readTree(line);
                String id = node.get("id").textValue();
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
