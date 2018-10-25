package com.sohu.tv.mq.cloud.web.vo;

/**
 * AuditAssociateConsumerVO
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月24日
 */
public class AuditAssociateConsumerVO {
    // 审核id
    private long aid;
    // topic id
    private long tid;
    // consumer id
    private long cid;
    private String user;
    private String topic;
    private String consumer;

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

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
