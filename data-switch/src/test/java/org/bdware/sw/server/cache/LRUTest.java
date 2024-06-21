package org.bdware.sw.server.cache;

import org.bdware.sw.nodemanager.cache.LRU.MyLRUCache;

public class LRUTest {
    public static void main(String[] args) {
        MyLRUCache<Integer, String> cache = new MyLRUCache<>(3);
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        System.out.println(cache); // 输出：{A, B, C}
        cache.get(2); // 访问key为2的元素
        System.out.println(cache); // 输出：{A, C, B}
        cache.put(4, "D"); // 添加新的元素，触发删除最久未被访问的元素
        System.out.println(cache); // 输出：{C, B, D}
        cache.decreaseCapacity();//减小容量，会删掉3=C
        System.out.println(cache);// 输出：{B,D}
        cache.put(5,"E");
        System.out.println(cache);// 输出：{D,E}
        cache.put(6,"F");
        System.out.println(cache);// 输出：{E,F}

    }

}
