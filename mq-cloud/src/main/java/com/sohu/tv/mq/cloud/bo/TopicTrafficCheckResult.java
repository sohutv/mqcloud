package com.sohu.tv.mq.cloud.bo;

/**
 * topic流量统计分析结果
 * @author yongweizhao
 * @create 2020/8/12 18:30
 */
public class TopicTrafficCheckResult {
    // topic id
    private long tid;
    // 流量异常时间点
    private String time;
    // 流量异常详情
    private String warnInfo;

    public TopicTrafficCheckResult() {}

    public TopicTrafficCheckResult(long tid, String time, String warnInfo) {
        this.tid = tid;
        this.time = time;
        this.warnInfo = warnInfo;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWarnInfo() {
        return warnInfo;
    }

    public void setWarnInfo(String warnInfo) {
        this.warnInfo = warnInfo;
    }
}
