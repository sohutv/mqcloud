package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * 反馈dao
 * 
 * @author yongfeigao
 * @date 2018年9月18日
 */
public class Feedback {

    private long id;
    private long uid;
    private String content;
    private Date createDate;

    // 扩展字段
    private User user;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Feedback [id=" + id + ", uid=" + uid + ", content=" + content + ", createDate=" + createDate + "]";
    }
}
