package org.bdware.sw.nodemanager.cache;

import org.bdware.sw.nodemanager.AddressOfSwitch;

import java.util.HashMap;
import java.util.Map;

public class GenerationalCache implements Cache{
    private int numGenerations; // 代数数量
    private Map<String, AddressOfSwitch> cache; // 缓存项
    private Map<String, Integer> frequencyCounters; // 计数器

    public GenerationalCache(int numGenerations) {
        this.numGenerations = numGenerations;
        cache = new HashMap<>();
        frequencyCounters = new HashMap<>();
    }

    public void put(String key, AddressOfSwitch value) {
        cache.put(key, value);
        frequencyCounters.put(key, numGenerations); // 放入最新的代数
    }

    public AddressOfSwitch get(String key) {
        AddressOfSwitch value = cache.get(key);
        if (value != null) {
            int currentFrequency = frequencyCounters.get(key);
            frequencyCounters.put(key, currentFrequency + 1); // 更新访问频率
        }
        return value;
    }

    public void evict() {
        int minCounter = Integer.MAX_VALUE;
        String minKey = null;

        for (Map.Entry<String, Integer> entry : frequencyCounters.entrySet()) {
            if (entry.getValue() < minCounter) {
                minCounter = entry.getValue();
                minKey = entry.getKey();
            }
        }

        cache.remove(minKey); // 替换访问频率最低的缓存项
        frequencyCounters.remove(minKey);
    }
}