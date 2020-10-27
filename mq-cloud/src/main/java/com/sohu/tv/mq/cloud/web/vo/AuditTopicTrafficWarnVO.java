package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.Topic;

/**
 * @author yongweizhao
 * @create 2020/9/24 15:33
 */
public class AuditTopicTrafficWarnVO {
    // topic
    private Topic topic;
    // 审核id
    private long aid;
    // topic id
    private long tid;
    // 流量预警是否开启
    private int trafficWarnEnabled;

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

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
