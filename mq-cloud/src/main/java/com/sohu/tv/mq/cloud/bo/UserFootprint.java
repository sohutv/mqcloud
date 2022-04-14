package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

/**
 * 用户足迹
 * @author: yongfeigao
 * @date: 2022/3/9 15:30
 */
public class UserFootprint {
    // id
    private long id;
    // uid
    private long uid;
    // tid
    private long tid;
    // Date
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

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "UserFootprint{" +
                "id=" + id +
                ", uid=" + uid +
                ", tid=" + tid +
                ", updateTime=" + updateTime +
                '}';
    }
}
