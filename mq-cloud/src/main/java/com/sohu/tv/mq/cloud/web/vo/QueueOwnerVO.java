package com.sohu.tv.mq.cloud.web.vo;
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
}
