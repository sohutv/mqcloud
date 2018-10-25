package com.sohu.tv.mq.cloud.bo;

/**
 * userConsumer删除审核
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月7日
 */
public class AuditUserConsumerDelete {
    // 审核id
    private long aid;
    // userConsumer id
    private long ucid;
    // user id
    private long uid;
    private String consumer;

    private String topic;

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getUcid() {
        return ucid;
    }

    public void setUcid(long ucid) {
        this.ucid = ucid;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}
