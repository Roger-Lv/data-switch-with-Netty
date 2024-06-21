package org.bdware.sw.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ParetoDistribution {

    public static void main(String[] args) {
        List<String> originalIds = generateIdList(); // 替换成你的id列表

        // 使用帕累托分布生成重复数据
        List<String> resultList = generateParetoDistribution(originalIds, 600000,2,2); // 是生成数据的总量,但可能更多
        // 打印生成的数据
        Collections.shuffle(resultList);
        System.out.println("Generated Data: " + resultList);
        List<String>  resultList2 = new ArrayList<>();
        // 引入时间窗口以保持时间局部性 1/2
        int num = resultList.size();
        int windowSize = Math.min(resultList.size()/6, num); // 选择适当的窗口大小
        for (int i = 0; i < num; i += windowSize) {
            int endIndex = Math.min(i + windowSize, num);
            List<String> subList = resultList.subList(i, endIndex);
            Collections.shuffle(subList);

            for (int j = 0; j < subList.size(); j++) {
                resultList2.add(subList.get(j));
            }
        }

        List<String>  resultList3 = new ArrayList<>();
        // 引入时间窗口以保持时间局部性
        int num2 = resultList2.size();
        int windowSize2 = Math.min(resultList2.size()/3, num); // 选择适当的窗口大小
        for (int i = 0; i < num; i += windowSize) {
            int endIndex = Math.min(i + windowSize, num);
            List<String> subList = resultList2.subList(i, endIndex);
            Collections.shuffle(subList);

            for (int j = 0; j < subList.size(); j++) {
                resultList3.add(subList.get(j));
            }
        }


        System.out.println("resultList size:" + resultList3.size());

        // 输出到json文件
        String outputIdListFile = "data-switch/src/test/java/org/bdware/sw/server/peretoIdList3.json";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputIdListFile));
            for (String value : resultList2) {
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

    private static List<String> generateParetoDistribution(List<String> originalIds, int totalDataCount, int minNum,double k) {
        // 计算每个id的权重，权重满足帕累托分布
        List<Double> weights = calculateParetoWeights(originalIds.size(),k);

        // 构造帕累托分布的重复数据
        List<String> duplicatedData = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < originalIds.size(); i++) {
            int size = (int) Math.max(totalDataCount*weights.get(i),minNum);
            for (int j= 0;j<size;j++){
                duplicatedData.add(originalIds.get(i));
            }

        }

        return duplicatedData;
    }

    private static List<Double> calculateParetoWeights(int size,double kk) {
        // 计算帕累托分布的权重
        List<Double> weights = new ArrayList<>();
        double k = kk; // 替换成合适的帕累托分布参数

        for (int i = 1; i <= size; i++) {
            double weight =0;
            if (i<=2){
                weight = 1 / Math.pow(i+1, k);
            }else{
                weight = 1 / Math.pow(i, k);
            }

            weights.add(weight);
        }

        // 归一化权重
        double sum = weights.stream().mapToDouble(Double::doubleValue).sum();
        weights = weights.stream().map(w -> w / sum).collect(Collectors.toList());

        return weights;
    }


}

