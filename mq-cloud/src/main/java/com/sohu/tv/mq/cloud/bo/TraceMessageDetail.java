package com.sohu.tv.mq.cloud.bo;

import java.util.Date;

import org.apache.rocketmq.client.trace.TraceType;
import org.apache.rocketmq.common.message.MessageType;

import com.sohu.tv.mq.cloud.util.DateUtil;

/**
 * trace消息详情
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2019年3月12日
 */
public class TraceMessageDetail {
    private TraceType traceType;
    private Long timeStamp;
    private String groupName;
    // 消费时间 ms
    private Integer costTime;
    private Boolean isSuccess;
    private String topic;
    private String msgId;
    private String keys;
    private String clientHost;
    private Integer retryTimes;
    private MessageType msgType;
    // 唯一id
    private String requestId;
    
    public TraceType getTraceType() {
        return traceType;
    }

    public void setTraceType(TraceType traceType) {
        this.traceType = traceType;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public String getTimeStampFormat() {
        if (timeStamp == null || timeStamp == 0) {
            return null;
        }
        return formatTime(timeStamp);
    }

    private String formatTime(long time) {
        return DateUtil.getFormat(DateUtil.YMD_DASH_HMS_COLON_DOT_SSS).format(new Date(time));
    }

    public void setTimeStamp(long timeStamp) {
        if (timeStamp == 0) {
            this.timeStamp = null;
        } else {
            this.timeStamp = timeStamp;
        }
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getCostTime() {
        return costTime;
    }

    public void setCostTime(int costTime) {
        if (costTime == 0) {
            this.costTime = null;
        } else {
            this.costTime = costTime;
        }
    }

    public Boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean isSuccess) {
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

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        if (retryTimes == 0) {
            this.retryTimes = null;
        } else {
            this.retryTimes = retryTimes;
        }
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public void setMsgType(MessageType msgType) {
        this.msgType = msgType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

}
