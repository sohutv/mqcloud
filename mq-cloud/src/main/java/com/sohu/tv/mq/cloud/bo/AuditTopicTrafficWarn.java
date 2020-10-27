package com.sohu.tv.mq.cloud.bo;

/**
 * @author yongweizhao
 * @create 2020/9/24 11:08
 */
public class AuditTopicTrafficWarn {
    // 审核id
    private long aid;
    // topic id
    private long tid;
    // topic traffic warn
    private int trafficWarnEnabled;

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

    public int getTrafficWarnEnabled() {
        return trafficWarnEnabled;
    }

    public void setTrafficWarnEnabled(int trafficWarnEnabled) {
        this.trafficWarnEnabled = trafficWarnEnabled;
    }
}
