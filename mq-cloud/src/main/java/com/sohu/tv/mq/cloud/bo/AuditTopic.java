package com.sohu.tv.mq.cloud.bo;

public class AuditTopic {
    // topic有序
    public static int HAS_ORDER = 1;
    // topic无序
    public static int NO_ORDER = 0;
    //审核id
    private long aid;
    //topic名
    private String name;
    //队列长度
    private int queueNum;
    //0:无序,1:有序
    private int ordered;
    // 生产者名
    private String producer;
    // 一天消息量，单位万
    private long qpd;
    // 每秒消息量
    private long qps;
    // 是否开启trace
    private int traceEnabled;

    public long getAid() {
        return aid;
    }

    public void setAid(long aid) {
        this.aid = aid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQueueNum() {
        return queueNum;
    }

    public void setQueueNum(int queueNum) {
        this.queueNum = queueNum;
    }

    public int getOrdered() {
        return ordered;
    }

    public void setOrdered(int ordered) {
        this.ordered = ordered;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public long getQpd() {
        return qpd;
    }

    public void setQpd(long qpd) {
        this.qpd = qpd;
    }

    public long getQps() {
        return qps;
    }

    public void setQps(long qps) {
        this.qps = qps;
    }
    
    public boolean traceEnabled() {
        return traceEnabled == 1;
    }
    
    public int getTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(int traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    @Override
    public String toString() {
        return "AuditTopic [aid=" + aid + ", name=" + name + ", queueNum=" + queueNum + ", ordered=" + ordered
                + ", producer=" + producer + ", qpd=" + qpd + ", qps=" + qps + "]";
    }

}
