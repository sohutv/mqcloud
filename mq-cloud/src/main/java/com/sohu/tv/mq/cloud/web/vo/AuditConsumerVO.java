package com.sohu.tv.mq.cloud.web.vo;
/**
 * AuditConsumerVO
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月24日
 */
public class AuditConsumerVO {
    //审核id
    private long aid;
    //topic id
    private long tid;
    //消费者名字
    private String consumer;
    //0:集群消费,1:广播消费
    private int consumeWay;
    private String topic;
    
    private int traceEnabled;
    
    private int permitsPerSecond;
    
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
    public String getConsumer() {
        return consumer;
    }
    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }
    public int getConsumeWay() {
        return consumeWay;
    }
    public void setConsumeWay(int consumeWay) {
        this.consumeWay = consumeWay;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public int getTraceEnabled() {
        return traceEnabled;
    }
    public void setTraceEnabled(int traceEnabled) {
        this.traceEnabled = traceEnabled;
    }
    public int getPermitsPerSecond() {
        return permitsPerSecond;
    }
    public void setPermitsPerSecond(int permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond;
    }
}
