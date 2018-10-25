package com.sohu.tv.mq.cloud.bo;

/**
 * consumer删除审核
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
public class AuditConsumerDelete {
    // 审核id
    private long aid;
    // consumer id
    private long cid;
    
    private String consumer;
    
    private String topic;
    
    public long getAid() {
        return aid;
    }
    public void setAid(long aid) {
        this.aid = aid;
    }
    public long getCid() {
        return cid;
    }
    public void setCid(long cid) {
        this.cid = cid;
    }
    public String getConsumer() {
        return consumer;
    }
    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
}
