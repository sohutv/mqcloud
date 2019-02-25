package com.sohu.tv.mq.cloud.web.vo;

import java.util.Date;

import org.apache.rocketmq.client.trace.TraceType;
import org.apache.rocketmq.common.message.MessageType;

import com.sohu.tv.mq.cloud.util.DateUtil;

/**
 * trace view vo
 * 
 * @author yongfeigao
 * @date 2019年2月25日
 */
public class TraceViewVO {
    private TraceType traceType;
    private long timeStamp;
    private String groupName;
    // 消费时间 ms
    private int costTime;
    private boolean isSuccess;
    private String topic;
    private String msgId;
    private String keys;
    private String clientHost;
    private int retryTimes;
    private MessageType msgType;

    public TraceType getTraceType() {
        return traceType;
    }

    public void setTraceType(TraceType traceType) {
        this.traceType = traceType;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getTimeStampFormat() {
        if(timeStamp == 0) {
            return null;
        }
        return formatTime(timeStamp);
    }

    private String formatTime(long time) {
        return DateUtil.getFormat(DateUtil.YMD_DASH_HMS_COLON_DOT_SSS).format(new Date(time));
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getCostTime() {
        return costTime;
    }

    public void setCostTime(int costTime) {
        this.costTime = costTime;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public void setMsgType(MessageType msgType) {
        this.msgType = msgType;
    }
}
