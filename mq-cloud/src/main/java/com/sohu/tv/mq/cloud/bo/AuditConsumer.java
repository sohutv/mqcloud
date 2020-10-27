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

    public int getTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(int traceEnabled) {
        this.traceEnabled = traceEnabled;
    }
    
    public String getName() {
        return consumer;
    }
    
    public int getPermitsPerSecond() {
        return permitsPerSecond;
    }

    public void setPermitsPerSecond(int permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond;
    }

    @Override
    public String toString() {
        return "AuditConsumer [aid=" + aid + ", tid=" + tid + ", consumer=" + consumer + ", consumeWay=" + consumeWay
                + ", traceEnabled=" + traceEnabled + ", permitsPerSecond=" + permitsPerSecond + "]";
    }
}
