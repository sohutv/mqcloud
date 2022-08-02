package com.sohu.tv.mq.cloud.bo;
/**
 * topic消费者
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月10日
 */
public class TopicConsumer {
    private long cid;
    private String consumer;
    private long tid;
    private String topic;
    private int clusterId;
    // 消费方式 0:集群消费,1:广播消费
    private int consumeWay;
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
    public long getTid() {
        return tid;
    }
    public void setTid(long tid) {
        this.tid = tid;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public int getClusterId() {
        return clusterId;
    }
    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public int getConsumeWay() {
        return consumeWay;
    }

    public void setConsumeWay(int consumeWay) {
        this.consumeWay = consumeWay;
    }
}
