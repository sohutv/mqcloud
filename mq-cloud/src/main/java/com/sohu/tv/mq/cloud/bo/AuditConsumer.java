package com.sohu.tv.mq.cloud.bo;

public class AuditConsumer {
    //审核id
    private long aid;
    //topic id
    private long tid;
    //消费者名字
    private String consumer;
    //0:集群消费,1:广播消费
    private int consumeWay;

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

    @Override
    public String toString() {
        return "AuditConsumer [aid=" + aid + ", tid=" + tid + ", consumer=" + consumer + ", consumeWay=" + consumeWay
                + "]";
    }
}
