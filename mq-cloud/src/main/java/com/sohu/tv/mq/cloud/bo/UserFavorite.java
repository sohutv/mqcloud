package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * 用户收藏
 * @author: yongfeigao
 * @date: 2022/3/21 15:34
 */
public class UserFavorite {
    // id
    private long id;
    // uid
    private long uid;
    // tid
    private long tid;
    // Date
    private Date createTime;

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "UserFavorite{" +
                "id=" + id +
                ", uid=" + uid +
                ", tid=" + tid +
                ", createTime=" + createTime +
                '}';
    }
}
