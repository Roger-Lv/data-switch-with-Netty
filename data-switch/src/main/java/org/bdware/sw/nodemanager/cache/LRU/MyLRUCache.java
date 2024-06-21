package org.bdware.sw.nodemanager.cache.LRU;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyLRUCache<K, V> {

    Map<K, Node> map;//缓存
    public int capacity;//最大缓存的数量
    public int size;
    private Node head = null;
    private Node tail = null;

    private class Node {
        public K key;
        public V value;
        public Node preNode;
        public Node nexNode;

        public Node(K key, V value, Node pre, Node next) {
            this.key = key;
            this.value = value;
            this.preNode = pre;
            this.nexNode = next;
        }
    }

    public MyLRUCache(int capacity) {
        this.size = 0;
        this.capacity = capacity;
        map = new HashMap<>(capacity);
    }

    public void add(K key, V value) {
        if (size != 0) {
            Node node = new Node(key, value, tail, null);
            tail.nexNode = node;
            tail = tail.nexNode;
        } else {
            this.head = new Node(key, value, null, null);
            this.tail = head;
        }
        size++;
    }

    public void removeHead(int index) {
        head.nexNode = null;
        Node afterNode = getIndexNode(1);
        afterNode.preNode = null;
    }

    public Node getIndexNode(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("索引越界");
        }
        //根据index大小来确定从头部查还是尾部查，以增加查询速度
        if (index < size / 2) {
            Node currentNode = head;
            for (int i = 0; i < size / 2 && currentNode != null; i++) {
                if (i == index) {
                    return currentNode;
                }
                currentNode = currentNode.nexNode;
            }
        } else {
            Node currentNode = tail;
            for (int i = size - 1; i >= size / 2 && currentNode != null; i--) {
                if (i == index) {
                    return currentNode;
                }
                currentNode = currentNode.preNode;
            }
        }
        return null;
    }


    public void put(K key, V value) {
        if (map.containsKey(key)) {
            Node node = map.get(key);
            node.value = value;
            return;
        } else if (map.size() >= capacity) { //若达到缓存上限则将距今最久的缓存删
            //从map中删除
            while(map.size()>=capacity){
                map.remove(head.key);
                //从链表中删除第一个
                Node afterNode = getIndexNode(1);
                head.nexNode = null;
                afterNode.preNode = null;
                head = afterNode;
                size--;
            }
        }
        add(key, value);
        map.put(key, tail);
    }

    public V get(K key) {
        if (map.containsKey(key)) {
            Node node = map.get(key);
            V value = node.value;
            remove(key);
            //将该节点增加到结尾
            add(key, value);
            map.put(key, tail);
            return value;
        }
        return null;
    }


    public void clear() {
        map.clear();
        // 将底层数组所有元素赋为null
        head = null;
        tail = null;
        size = 0;
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public void remove(K key) {

        if (containsKey(key)) {
            Node node = map.get(key);
            //删除该节点
            map.remove(key);
            Node beforeNode = node.preNode;
            Node afterNode = node.nexNode;
            node.nexNode = null;
            node.preNode = null;
            if (null != beforeNode) {
                beforeNode.nexNode = afterNode;
            } else {
                head = afterNode;
            }
            if (null != afterNode) {
                afterNode.preNode = beforeNode;
            } else {
                tail = beforeNode;
            }
            size--;
        }
    }

    public void decreaseCapacity(){
        this.capacity--;
        //从map中删除
        while(map.size()>capacity){
            map.remove(head.key);
            //从链表中删除第一个
            Node afterNode = getIndexNode(1);
            head.nexNode = null;
            afterNode.preNode = null;
            head = afterNode;
            size--;
        }
    }

    @Override
    public String toString() {
        String[] array = new String[size];
        Node currentNode = head;
        for (int i = 0; i < size; i++) {
            array[i] = String.valueOf(currentNode.value);
            currentNode = currentNode.nexNode;
        }
        return Arrays.toString(array);
    }
}
