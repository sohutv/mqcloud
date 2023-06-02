package com.sohu.tv.mq.cloud.web.vo;
/**
 * AuditAssociateProducerVO
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月23日
 */
public class AuditAssociateProducerVO {
    // 审核id
    private long aid;
    // topic id
    private long tid;
    
    private String user;
    // producer
    private String producer;
    private String topic;
    // audit type
    private Integer type;
    // 是否开启http生产
    private int httpEnabled;
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
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public int getHttpEnabled() {
        return httpEnabled;
    }

    public void setHttpEnabled(int httpEnabled) {
        this.httpEnabled = httpEnabled;
    }
}
