package com.sohu.tv.mq.cloud.cache;

import java.util.HashMap;
import java.util.Map;

import com.google.common.cache.CacheStats;
import com.sohu.tv.mq.util.JSONUtil;

/**
 * LocalCacheStats
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月22日
 */
@SuppressWarnings({"rawtypes"})
public class LocalCacheStats implements CacheStatsMbean {
    
    private LocalCache localCache;
    
    public LocalCacheStats(LocalCache localCache) {
        this.localCache = localCache;
    }

    public Map<String, String> getStats() {
        Map<String, String> map = new HashMap<String, String>();
        CacheStats stats = localCache.getCache().stats();
        map.put("size", String.valueOf(localCache.getCache().size()));
        map.put("hitCount", String.valueOf(stats.hitCount()));
        map.put("hitRate", String.valueOf(stats.hitRate()));
        map.put("evictionCount", String.valueOf(stats.hitRate()));
        map.put("missCount", String.valueOf(stats.missCount()));
        map.put("loadSuccessCount", String.valueOf(stats.loadSuccessCount()));
        map.put("loadExceptionCount", String.valueOf(stats.loadExceptionCount()));
        map.put("totalLoadTime", String.valueOf(stats.totalLoadTime()));
        map.put("evictionCount", String.valueOf(stats.evictionCount()));
        return map;
    }
    
    @Override
    public Object getValue(String key) {
        Object obj = localCache.get(key);
        if(obj instanceof String) {
            return obj;
        }
        return JSONUtil.toJSONString(obj);
    }

    @Override
    public void clear() {
        localCache.cleanUp();
    }
}