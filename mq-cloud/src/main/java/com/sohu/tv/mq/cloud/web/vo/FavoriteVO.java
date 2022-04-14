package com.sohu.tv.mq.cloud.web.vo;

import java.util.Date;

/**
 * 收藏vo
 * @author: yongfeigao
 * @date: 2022/3/21 16:21
 */
public class FavoriteVO {
    private long tid;
    private String topic;
    private Date createTime;

    public FavoriteVO() {
    }

    public FavoriteVO(long tid, Date createTime) {
        this.tid = tid;
        this.createTime = createTime;
    }

    public FavoriteVO(long tid, String topic) {
        this.tid = tid;
        this.topic = topic;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
