package com.bbzhu.cache;

import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Administrator on 2016/9/19.
 *
 */
public class Cache {
    private Cache(){};
    private Map<Object,Object> data = new ConcurrentHashMap<Object,Object>();
    private final static Cache instance = new Cache();

    public static Cache getInstance() {
        return instance;
    }

    public Object get(Object key) {
        return data.get(key);
    }

    public void set(Object key , Object value) {
        data.put(key , value);
    }

    public void clean() {
        data.clear();
    }

    public Object test() {
        return data;
    }

    public void SetAll(ConcurrentHashMap<Object,Object> map) {
        data = map;
    }
}
