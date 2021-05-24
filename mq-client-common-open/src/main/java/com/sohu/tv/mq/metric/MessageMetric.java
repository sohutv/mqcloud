package com.sohu.tv.mq.metric;

import java.util.List;

/**
 * 消息统计
 * 
 * @author yongfeigao
 * @date 2021年4月14日
 */
public class MessageMetric {
    // 开始时间
    private long startTime;
    // 消费的消息id
    private List<String> msgIdList;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public List<String> getMsgIdList() {
        return msgIdList;
    }

    public void setMsgIdList(List<String> msgIdList) {
        this.msgIdList = msgIdList;
    }
}