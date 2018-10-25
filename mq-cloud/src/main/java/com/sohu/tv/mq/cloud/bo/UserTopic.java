package com.sohu.tv.mq.cloud.bo;

/**
 * 用户topic
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月12日
 */
public abstract class UserTopic {
    private long id;
    private long uid;
    private long tid;
    
    // 冗余字段
    private String username;

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

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserTopic [id=" + id + ", uid=" + uid + ", tid=" + tid + "]";
    }
}
