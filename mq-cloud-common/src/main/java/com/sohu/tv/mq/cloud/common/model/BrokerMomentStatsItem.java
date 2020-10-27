package com.sohu.tv.mq.cloud.common.model;
/**
 * broker瞬时数据项
 * 
 * @author yongfeigao
 * @date 2020年7月9日
 */
public class BrokerMomentStatsItem {
    /**
     * key格式为：queueId@topic@consumerGroup
     */
    private String key;
    
    // 瞬时值
    private long value;
    
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public long getValue() {
        return value;
    }
    public void setValue(long value) {
        this.value = value;
    }
}
