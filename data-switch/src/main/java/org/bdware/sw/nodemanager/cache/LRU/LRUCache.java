package org.bdware.sw.nodemanager.cache.LRU;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private int cap;
    private static final long serialVersionUID = 1L;

    public LRUCache(int cap) {
        super(16, 0.75f, true);
        this.cap = cap;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > cap;
    }
}

