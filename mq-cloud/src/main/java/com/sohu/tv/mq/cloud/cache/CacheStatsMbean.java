package com.sohu.tv.mq.cloud.cache;

import java.util.Map;

/**
 * DMCacheStats mbean
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月22日
 */
public interface CacheStatsMbean {
    
    /**
     * 获取状态
     * @return
     */
    public Map<String, String> getStats();
    
    /**
     * 获取数据
     * @param key
     * @return
     */
    public Object getValue(String key);
    
    /**
     * 清空数据
     */
    public void clear();
}
