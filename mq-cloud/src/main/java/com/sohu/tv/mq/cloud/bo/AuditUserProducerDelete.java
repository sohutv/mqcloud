package com.sohu.tv.mq.cloud.bo;

/**
 * userProducer删除审核
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月5日
 */
public class AuditUserProducerDelete {
    // 审核id
    private long aid;
    // userProducer id
    private long pid;
    // user id
    private long uid;

    private String producer;

    private String topic;

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}
