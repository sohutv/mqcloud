package com.sohu.tv.mq.cloud.web.vo;

import java.util.Date;

import com.sohu.tv.mq.cloud.bo.Audit.StatusEnum;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.User;

/**
 * 审核vo
 * 
 * @author yumeiwang
 */
public class AuditVO {
    // 审核id
    private Long id;
    // 用户uid
    private long uid;
    // 用户
    private User user;
    // 申请类型:0:新建topic,1:修改topic,2:删除topic,3:新建消费者,4:删除消费者,5:重置offset
    private TypeEnum typeEnum;
    // 申请描述
    private String info;
    // 0:等待审批,1:审批通过,2:驳回
    private StatusEnum statusEnum;
    // 驳回理由
    private String refuseReason;

    // 审计员(邮箱)
    private String auditor;

    private Date createTime;

    private Date updateTime;

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getRefuseReason() {
        return refuseReason;
    }

    public void setRefuseReason(String refuseReason) {
        this.refuseReason = refuseReason;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public StatusEnum getStatusEnum() {
        return statusEnum;
    }

    public void setStatusEnum(StatusEnum statusEnum) {
        this.statusEnum = statusEnum;
    }
}
