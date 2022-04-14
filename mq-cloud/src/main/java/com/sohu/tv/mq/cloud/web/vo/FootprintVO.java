package com.sohu.tv.mq.cloud.web.vo;

import java.util.Date;

/**
 * 足迹vo
 *
 * @author: yongfeigao
 * @date: 2022/3/9 16:38
 */
public class FootprintVO {
    private long tid;
    private String topic;
    private Date updateTime;

    public FootprintVO() {
    }

    public FootprintVO(long tid, Date updateTime) {
        this.tid = tid;
        this.updateTime = updateTime;
    }

    public FootprintVO(long tid, String topic) {
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
