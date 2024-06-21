package org.bdware.sw.server.cache;

import org.bdware.sw.nodemanager.AddressOfSwitch;
import org.bdware.sw.nodemanager.cache.LFU.LFUCache;

public class LFUTest {

    public static void main(String[] args){
        LFUCache lfuCache = new LFUCache(3);

        // 添加键值对
        lfuCache.put("key1", new AddressOfSwitch("value1","1",1));
        lfuCache.put("key2", new AddressOfSwitch("value2","2",2));
        lfuCache.put("key3", new AddressOfSwitch("value3","3",3));

        // 打印当前缓存大小
//        System.out.println("Current Cache Size: " + lfuCache.getSize());

        // 获取缓存项
        System.out.println("Get key1: " + lfuCache.get("key1"));  // Output: value1

        // 更新使用次数
        lfuCache.get("key2");
        lfuCache.get("key2");

        // 打印当前缓存大小
//        System.out.println("Current Cache Size: " + lfuCache.getSize());

        // 添加新的键值对，触发淘汰最不经常使用的键
        lfuCache.put("key4", new AddressOfSwitch("value4","4",4));

        // 打印当前缓存大小和是否包含被淘汰的键
//        System.out.println("Current Cache Size: " + lfuCache.getSize());
        System.out.println("Contains key3: " + lfuCache.get("key3"));
        // 减小其大小,这里会删除掉第四个
//        lfuCache.decreaseCapacity();
//        System.out.println("Current Cache Size: " + lfuCache.getSize());
    }
}
