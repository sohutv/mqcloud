package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.Topic;
/**
 * topic删除修改
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
public class AuditTopicUpdateVO {
    // topic
    private Topic topic;
    // 审核id
    private long aid;
    // topic di
    private long tid;
    // 队列数量
    private int queueNum;
    
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
    public int getQueueNum() {
        return queueNum;
    }
    public void setQueueNum(int queueNum) {
        this.queueNum = queueNum;
    }
}
