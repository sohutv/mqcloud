package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.util.CommonUtil;

/**
 * 队列的消费者
 * 
 * @author yongfeigao
 * @date 2018年12月21日
 */
public class QueueOwnerVO {
    private String brokerName;
    private int queueId;
    private String clientId;
    private String topic;
    public String getBrokerName() {
        return brokerName;
    }
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }
    public int getQueueId() {
        return queueId;
    }
    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public int getTopicType() {
        if(CommonUtil.isRetryTopic(topic)) {
            return 1;
        }
        if(CommonUtil.isDeadTopic(topic)) {
            return 2;
        }
        return 0;
    }
}
