package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.Topic;
/**
 * topic trace修改
 * 
 * @author yongfeigao
 * @date 2019年11月18日
 */
public class AuditTopicTraceVO {
    // topic
    private Topic topic;
    // 审核id
    private long aid;
    // topic id
    private long tid;
    // trace启用
    private int traceEnabled;
    
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
    public int getTraceEnabled() {
        return traceEnabled;
    }
    public void setTraceEnabled(int traceEnabled) {
        this.traceEnabled = traceEnabled;
    }
}
