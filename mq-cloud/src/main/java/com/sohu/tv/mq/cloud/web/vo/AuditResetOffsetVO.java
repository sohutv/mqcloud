package com.sohu.tv.mq.cloud.web.vo;
/**
 * 重置偏移量
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月26日
 */
public class AuditResetOffsetVO {
    // 审核id
    private long aid;
    // 审核id
    private long tid;
    // 消费者id
    private long consumerId;
    // null:重置为最大offset,时间戳:重置为某时间点(yyyy-MM-dd#HH:mm:ss:SSS)
    private String offset;
    private String topic;
    private String consumer;
    private String messageKey;
    public long getAid() {
        return aid;
    }
    public void setAid(long aid) {
        this.aid = aid;
    }
    public long getTid() {
        return tid;
    }
    public void setTid(long tid) {
        this.tid = tid;
    }
    public long getConsumerId() {
        return consumerId;
    }
    public void setConsumerId(long consumerId) {
        this.consumerId = consumerId;
    }
    public String getOffset() {
        return offset;
    }
    public void setOffset(String offset) {
        this.offset = offset;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getConsumer() {
        return consumer;
    }
    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }
    public String getMessageKey() {
        return messageKey;
    }
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
}
