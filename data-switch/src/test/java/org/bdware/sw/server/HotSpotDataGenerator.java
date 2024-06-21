package org.bdware.sw.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

import java.util.ArrayList;
import java.util.List;

public class HotSpotDataGenerator {
    public static final int HOTSPOT_DATA_RATIO = 20; // 热点数据比例

    public static void main(String[] args) {
        List<String> idList = generateIdList(); // 生成初始ID列表
        Collections.shuffle(idList);
        List<String> hotSpotDataList = generateHotSpotData(idList); // 构造热点数据列表
        List<String> nonHotSpotDataList = generateNonHotSpotData(idList); // 构造非热点数据列表

        List<String> resultList = new ArrayList<>();
        for (String id:hotSpotDataList){
            resultList.add(id);
        }
        for (String id:nonHotSpotDataList){
            resultList.add(id);
        }
        System.out.println("hotSpotDataList size:"+hotSpotDataList.size());
        System.out.println("nonHotSpotDataList size:"+nonHotSpotDataList.size());
        double size1= hotSpotDataList.size();
        double size2= nonHotSpotDataList.size();
        System.out.println("hot/nonHot ratio: "+size1/size2);
        System.out.println("resultList size:"+resultList.size());
        Collections.shuffle(resultList);
        //输出到json文件
        String outputIdListFile = "data-switch/src/test/java/org/bdware/sw/server/outputIdList2.json";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputIdListFile));
            for (String value : resultList) {
                String json = "{\"id\":\"" + value + "\"}";
                writer.write(json);
                writer.newLine();
            }
            writer.close();
            System.out.println("Data has been written to " + outputIdListFile + " successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
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

    // 构建热点数据数据列表
    public static List<String> generateHotSpotData(List<String> idList) {
        List<String> hotSpotDataList = new ArrayList<>();

        int hotSpotDataSize = idList.size() * HOTSPOT_DATA_RATIO / 100;

        for (int i = idList.size()-hotSpotDataSize; i < idList.size(); i++) {
            int random = (int) (32*Math.random());
            for (int j=0;j<random;j++){
                System.out.println(j);
                hotSpotDataList.add(idList.get(i));
            }
            System.out.println(i);
        }

        return hotSpotDataList;
    }

    // 构造非热点数据列表
    public static List<String> generateNonHotSpotData(List<String> idList) {
        List<String> nonHotSpotDataList = new ArrayList<>();

        int nonHotSpotDataSize = idList.size() - (idList.size() * HOTSPOT_DATA_RATIO / 100);

        for (int i=0;i<nonHotSpotDataSize;i++) {
            int random = (int) (3*Math.random());
            for (int j=0;j<random;j++)
            nonHotSpotDataList.add(idList.get(i));
        }

        return nonHotSpotDataList;
    }
}