package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * 用户消息
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
public class UserMessage {
    private long id;
    private long uid;
    private String message;
    // 0:未读，1:已读
    private int status;
    private Date createDate;
    private Date updateTime;
    
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getUid() {
        return uid;
    }
    public void setUid(long uid) {
        this.uid = uid;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public int getStatus() {
        return status;
    }
    public Date getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public Date getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    @Override
    public String toString() {
        return "UserMessage [id=" + id + ", uid=" + uid + ", message=" + message + ", status=" + status
                + ", createDate=" + createDate + ", updateTime=" + updateTime + "]";
    }
}
