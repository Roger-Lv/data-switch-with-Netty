package org.bdware.sw.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ParetoHotSpotDataGenerator {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        List<String> idList = generateIdList();
        List<String> resultList = new ArrayList<>();
        List<String> resultList2 = new ArrayList<>();
        int n = idList.size(); // 获取list中的元素个数
        double xMin = 1.0; // 帕累托分布的最小值
        double k = 2.0; // 帕累托分布的形状参数

        Map<String, Integer> paretoMap = new LinkedHashMap<>();
        int sum = 0;
        for (int i = 0; i < n; i++) {
            double p=0;
            if (i==n-1){
                p = (double) (i) / n; // 根据位置计算概率
            }else{
                p = (double) (i + 1) / n; // 根据位置计算概率
            }
            int value = (int) Math.ceil(xMin / Math.pow(1 - p, 1 / k)); // 计算对应位置的value
            paretoMap.put(idList.get(i), value);
        }

        // 输出map中的键值对
        for (Map.Entry<String, Integer> entry : paretoMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        for (int i=0;i<n;i++){
            int size = paretoMap.get(idList.get(i));
            for (int j=0;j<size;j++){
                resultList.add(idList.get(i));
            }
        }

        // 引入时间窗口以保持时间局部性
        int num = resultList.size();
        int windowSize = Math.min(500000, num); // 选择适当的窗口大小
        for (int i = 0; i < num; i += windowSize) {
            int endIndex = Math.min(i + windowSize, num);
            List<String> subList = resultList.subList(i, endIndex);
            Collections.shuffle(subList);

            for (int j = 0; j < subList.size(); j++) {
                resultList2.add(subList.get(j));
            }
        }


        System.out.println("resultList size:" + resultList2.size());

        // 输出到json文件
        String outputIdListFile = "data-switch/src/test/java/org/bdware/sw/server/peretoIdList2.json";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputIdListFile));
            for (String value : resultList) {
                String json = "{\"id\":\"" + value + "\"}";
                writer.write(json);
                writer.newLine();
            }
            writer.close();
            System.out.println("数据已成功写入文件：" + outputIdListFile);
        } catch (IOException e) {
            System.out.println("写入文件时发生错误：" + e.getMessage());
        }
    }

    // 生成初始ID列表
    public static List<String> generateIdList(){
        List<String> idList = new ArrayList<>();
        //GitHub
        String filePath = "data-switch/src/test/java/org/bdware/sw/server/puregithubid.json";
        ObjectMapper mapper = new ObjectMapper();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode node = mapper.readTree(line);
                String id = node.get("id").textValue();
                idList.add(id);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //CSTR
        filePath = "data-switch/src/test/java/org/bdware/sw/server/casdatacenter(1).jsonl";
        mapper = new ObjectMapper();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode node = mapper.readTree(line);
                JsonNode node1 = node.get("desc");
                if (node1.get("cstr")==null)continue;
                String id=node1.get("cstr").textValue();
                if (id==null)continue;
                if (id.startsWith("CSTR:")){
                    idList.add(id);
                }else if (id.startsWith("https://cstr.cn/")){
                    id=id.replaceAll("https://cstr.cn/","CSTR:");
                    idList.add(id);
                }else if(id.startsWith("cstr:")){
                    id=id.replaceAll("cstr:","CSTR:");
                    idList.add(id);
                }

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //DOI
        filePath = "data-switch/src/test/java/org/bdware/sw/server/puredoi.json";
        mapper = new ObjectMapper();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode node = mapper.readTree(line);
                String id = node.get("id").textValue();
                idList.add(id);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //IDS
        filePath = "data-switch/src/test/java/org/bdware/sw/server/pure_ids_id.json";
        mapper = new ObjectMapper();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode node = mapper.readTree(line);
                String id = node.get("id").textValue();
                idList.add(id);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("size of idList:"+idList.size());

        return idList;
    }
}
