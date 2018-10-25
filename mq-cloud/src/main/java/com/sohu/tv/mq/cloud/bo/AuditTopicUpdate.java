package com.sohu.tv.mq.cloud.bo;

/**
 * topic审核删除
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
public class AuditTopicUpdate {
    // 审核id
    private long aid;
    // topic di
    private long tid;
    // 队列数量
    private int queueNum;
    
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
