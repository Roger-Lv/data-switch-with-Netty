package org.bdware.sw.nodemanager.cache.SLRU;

import org.bdware.sw.nodemanager.AddressOfSwitch;
import org.bdware.sw.nodemanager.cache.Cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class SLRUCache implements Cache {
    private final LinkedHashMap<String, AddressOfSwitch> hotCache;
    private final LinkedHashMap<String, AddressOfSwitch> coldCache;
    private final int hotSize;
    private final int coldSize;

    public SLRUCache(int size) {
        this.hotSize = (int) (size*0.05);
        this.coldSize = size-hotSize;
        this.hotCache = new LinkedHashMap<>(hotSize, 0.75f, true);
        this.coldCache = new LinkedHashMap<String, AddressOfSwitch>(coldSize, 0.75f, true){
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, AddressOfSwitch> eldest) {
                return size()>coldSize;
            }
        };;
    }

    public void put(String key, AddressOfSwitch value) {
        if (hotCache.containsKey(key)) {
            hotCache.put(key, value);
            if (hotCache.size() > hotSize) {
                String coldest = hotCache.keySet().iterator().next();
                AddressOfSwitch coldestValue = hotCache.remove(coldest);
                coldCache.put(coldest, coldestValue);
            }
        } else if (coldCache.containsKey(key)) {
            coldCache.remove(key);
            hotCache.put(key, value);
            if (hotCache.size() > hotSize) {
                String coldest = hotCache.keySet().iterator().next();
                AddressOfSwitch coldestValue = hotCache.remove(coldest);
                coldCache.put(coldest, coldestValue);
            }
        } else {
            coldCache.put(key, value);
        }
    }

    public AddressOfSwitch get(String key) {

        if (hotCache.containsKey(key)) {
            AddressOfSwitch addressOfSwitch=hotCache.get(key);
            return addressOfSwitch;
        } else if (coldCache.containsKey(key)) {
            AddressOfSwitch value = coldCache.get(key);
            coldCache.remove(key);
            hotCache.put(key, value);
            if (hotCache.size() > hotSize) {
                String coldest = hotCache.keySet().iterator().next();
                AddressOfSwitch coldestValue = hotCache.remove(coldest);
                coldCache.put(coldest, coldestValue);
            }
            return value;
        }
        return null;
    }

    public void remove(String key) {
        if (hotCache.containsKey(key)) {
            hotCache.remove(key);
        } else if (coldCache.containsKey(key)) {
            coldCache.remove(key);
        }
    }

    private void removeCold() {
        if (!coldCache.isEmpty()) {
            String coldest = coldCache.keySet().iterator().next();
            coldCache.remove(coldest);
        }
    }
}
