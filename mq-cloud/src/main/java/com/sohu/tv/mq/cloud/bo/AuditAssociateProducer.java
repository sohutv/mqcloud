package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.util.MQProtocol;

/**
 * 审核关联生产者
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月17日
 */
public class AuditAssociateProducer {
    // 审核id
    private long aid;
    // topic id
    private long tid;
    //uid
    private long uid;
    // producer
    private String producer;
    // 通信协议
    private int protocol;
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
    @Override
    public String toString() {
        return "AuditAssociateProducer [aid=" + aid + ", uid="+ uid +", tid=" + tid + ", producer=" + producer + "]";
    }
    public long getUid() {
        return uid;
    }
    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public boolean isProxyRemoting() {
        return MQProtocol.isProxyRemoting(protocol);
    }
}
