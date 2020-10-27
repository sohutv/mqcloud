package com.sohu.tv.mq.cloud.bo;

/**
 * topic trace审核
 * 
 * @author yongfeigao
 * @date 2019年11月18日
 */
public class AuditTopicTrace {
    // 审核id
    private long aid;
    // topic id
    private long tid;
    
    private int traceEnabled;
    
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
    public int getTraceEnabled() {
        return traceEnabled;
    }
    public void setTraceEnabled(int traceEnabled) {
        this.traceEnabled = traceEnabled;
    }
}
