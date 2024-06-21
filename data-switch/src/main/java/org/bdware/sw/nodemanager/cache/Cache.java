package org.bdware.sw.nodemanager.cache;

import org.bdware.sw.nodemanager.AddressOfSwitch;

public interface Cache {
    public void put(String key,AddressOfSwitch addressOfSwitch);
    public AddressOfSwitch get(String key);
}
