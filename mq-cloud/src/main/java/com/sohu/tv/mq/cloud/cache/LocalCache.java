package com.sohu.tv.mq.cloud.cache;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 本地缓存
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月13日
 */
public class LocalCache<T> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // 缓存
    private Cache<String, T> cache;
    // 缓存名称
    private String name;
    // 缓存大小
    private int size;
    // 过期时间,单位秒
    private int expireAfterAccess;
    // 过期时间,单位秒
    private int expireAfterWrite;

    /**
     * 缓存初始化
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void init() {
        CacheBuilder cacheBuilder = CacheBuilder.newBuilder().maximumSize(size).recordStats();
        if(expireAfterAccess > 0) {
            cacheBuilder.expireAfterAccess(expireAfterAccess, TimeUnit.SECONDS);
        }
        if(expireAfterWrite > 0) {
            cacheBuilder.expireAfterWrite(expireAfterWrite, TimeUnit.SECONDS);
        }
        cache = cacheBuilder.build();
    }

    /**
     * 获取对象
     * 
     * @param key
     * @return
     */
    public T get(String key) {
        T obj = null;
        try {
            obj = cache.getIfPresent(key);
        } catch (Exception e) {
            logger.error("get err, key:", key, e);
        }
        return obj;
    }

    /**
     * 缓存对象
     * 
     * @param key
     * @param value
     */
    public void put(String key, T value) {
        try {
            cache.put(key, value);
        } catch (Exception e) {
            logger.error("put err, key:{}", key, e);
        }
    }

    /**
     * 清除缓存
     */
    public void cleanUp() {
        try {
            cache.invalidateAll();
            logger.info("local cache invalidateAll ok!");
        } catch (Exception e) {
            logger.error("cleanUp err", e);
        }
    }
    
    /**
     * 清除缓存
     */
    public void cleanUp(String key) {
        try {
            cache.invalidate(key);
            logger.info("local cache invalidate {} ok!", key);
        } catch (Exception e) {
            logger.error("cleanUp {} err", key, e);
        }
    }

    public Cache<String, T> getCache() {
        return cache;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getExpireAfterAccess() {
        return expireAfterAccess;
    }

    public void setExpireAfterAccess(int expireAfterAccess) {
        this.expireAfterAccess = expireAfterAccess;
    }

    public int getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(int expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }
}
