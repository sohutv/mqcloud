package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * @author fengwang219475
 * @version 1.0
 * @project mqcloud
 * @description 定时消息取消
 * @date 2023/7/5 18:03:49
 */
public class AuditWheelMessageCancel {


    /**
     * 申请取消 最小时间范围  delayTime - nowTime > DEFAULT_EXPIRE_CANCEL_TIME
     */
    public static final long DEFAULT_EXPIRE_CANCEL_TIME = 5 * 60 * 1000L;

    /**
     * 执行取消操作 最小时间范围  delayTime - nowTime > MINOR_EXPIRE_CANCEL_TIME
     */
    public static final long MINOR_EXPIRE_CANCEL_TIME = 3 * 1000L;

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 申请用户ID
     */
    private Long uid;

    /**
     * 审核记录ID
     */
    private Long aid;

    /**
     * Topic ID
     */
    private Long tid;

    /**
     * 待发送取消消息的目标broker
     */
    private String brokerName;

    /**
     * Message ID
     */
    private String uniqueId;

    /**
     * 延迟时间
     */
    private Long deliverTime;

    /**
     * 创建时间
     */
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getAid() {
        return aid;
    }

    public void setAid(Long aid) {
        this.aid = aid;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Long getDeliverTime() {
        return deliverTime;
    }

    public void setDeliverTime(Long deliverTime) {
        this.deliverTime = deliverTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "AuditWheelMessageCancel{" +
                "id=" + id +
                ", aid=" + aid +
                ", tid=" + tid +
                ", uniqueId='" + uniqueId + '\'' +
                ", deliverTime=" + deliverTime +
                '}';
    }
}
