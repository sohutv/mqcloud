package com.sohu.tv.mq.cloud.bo;
/**
 * 审核关联消费者
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月17日
 */
public class AuditAssociateConsumer {
    // 审核id
    private long aid;
    // topic id
    private long tid;
    // 用户ID
    private long uid;
    // consumer id
    private long cid;
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
    public long getCid() {
        return cid;
    }
    public void setCid(long cid) {
        this.cid = cid;
    }
    @Override
    public String toString() {
        return "AuditAssociateConsumer [aid=" + aid + ", tid=" + tid + ", uid="+ uid +", cid=" + cid + "]";
    }
    public long getUid() {
        return uid;
    }
    public void setUid(long uid) {
        this.uid = uid;
    }
}
