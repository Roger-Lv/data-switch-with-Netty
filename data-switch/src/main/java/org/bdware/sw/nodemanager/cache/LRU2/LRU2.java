package org.bdware.sw.nodemanager.cache.LRU2;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRU2<K, V> {
    private final LinkedHashMap<K, V> hotLRU;
    private final LinkedHashMap<K, V> coldLRU;

    private int size;
    private int hotLRUSize;
    private int maxSize;

    public LRU2(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        hotLRU = new LinkedHashMap<>(0, 0.75f, true);
        coldLRU = new LinkedHashMap<>(0, 0.75f, true);

        this.maxSize = maxSize;
    }

    public final V get(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        V value;
        synchronized (this) {
            value = hotLRU.get(key);
            if (value != null) {
                return value;
            }
            value = coldLRU.get(key);
            if (value != null) {
                upgrade(key, value);
                return value;
            }
            return null;
        }
    }

    private void upgrade(K key, V value) {
        coldLRU.remove(key);
        putMajor(key, value);
    }

    private void putMajor(K key, V value) {
        hotLRUSize += safeSizeOf(key, value);
        hotLRU.put(key, value);
        downgrade();
    }


    private void downgrade() {
        //the ratio of hotLRUSize and coldLRU can be changed, to get the best performance the ratio may need to be changed
        //in famous project "Caffiene" the ratio of hotLRU:coldLRU is 4:1, but maybe it isn't the best ratio
        //here I simply set a ratio
        if (!hotLRU.isEmpty() && hotLRUSize > maxSize *0.02) {
            Map.Entry<K, V> next = hotLRU.entrySet().iterator().next();
            K downgradeKey = next.getKey();
            V downgradeValue = next.getValue();
            hotLRUSize -= safeSizeOf(downgradeKey, downgradeValue);
            hotLRU.remove(downgradeKey);
            coldLRU.put(downgradeKey, downgradeValue);
        }
    }

    public final void put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        V mapValue;
        synchronized (this) {
            mapValue = hotLRU.get(key);
            if (mapValue != null) {
                putMajor(key, value);
            } else {
                mapValue = coldLRU.get(key);
                if (mapValue != null) {
                    upgrade(key, value);
                } else {
                    // not in hotLRU or coldLRU
                    coldLRU.put(key, value);
                }
            }
            size += safeSizeOf(key, value) - safeSizeOf(key, mapValue);
        }
        trim();
    }

    private void trim() {
        while (size > maxSize) {
            if (hotLRU.isEmpty() && coldLRU.isEmpty()) {
                break;
            }
            K key;
            V value;
            Map.Entry<K, V> next;
            synchronized (this) {
                if (coldLRU.entrySet().iterator().hasNext()) {
                    next = coldLRU.entrySet().iterator().next();
                    key = next.getKey();
                    value = next.getValue();
                    coldLRU.remove(key);
                } else {
                    next = hotLRU.entrySet().iterator().next();
                    key = next.getKey();
                    value = next.getValue();
                    hotLRU.remove(key);
                    hotLRUSize -= safeSizeOf(key, value);
                }
                size -= safeSizeOf(key, value);
            }
        }
    }

    private int safeSizeOf(K key, V value) {
        int result = sizeOf(key, value);
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + key + "=" + value);
        }
        return result;
    }

    protected int sizeOf(K key, V value) {
        if (value == null){
            return 0;
        }
        return 1;
    }

    public synchronized final Map<K, V> snapshotLRUMajor() {
        return new LinkedHashMap<>(hotLRU);
    }

    public synchronized final Map<K, V> snapshotLRUMinor() {
        return new LinkedHashMap<>(coldLRU);
    }
}