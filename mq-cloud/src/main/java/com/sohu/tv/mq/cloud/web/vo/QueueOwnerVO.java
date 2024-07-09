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
    private String ip;

    // 客户端警告信息
    private String warnInfo;
    // 是否显示客户端信息
    private boolean showClientInfo;
    // 语言
    private String language;

    private boolean paused;

    private boolean disablePause;

    // 是否消费失败
    private boolean consumeFailed;

    // 是否流控
    private boolean flowControlled;

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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isDisablePause() {
        return disablePause;
    }

    public void setDisablePause(boolean disablePause) {
        this.disablePause = disablePause;
    }

    public int getTopicType() {
        if (CommonUtil.isRetryTopic(topic)) {
            return 1;
        }
        if (CommonUtil.isDeadTopic(topic)) {
            return 2;
        }
        return 0;
    }

    public String getWarnInfo() {
        return warnInfo;
    }

    public void setWarnInfo(String warnInfo) {
        this.warnInfo = warnInfo;
    }

    public boolean isShowClientInfo() {
        return showClientInfo;
    }

    public void setShowClientInfo(boolean showClientInfo) {
        this.showClientInfo = showClientInfo;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isConsumeFailed() {
        return consumeFailed;
    }

    public void setConsumeFailed(boolean consumeFailed) {
        this.consumeFailed = consumeFailed;
    }

    public boolean isFlowControlled() {
        return flowControlled;
    }

    public void setFlowControlled(boolean flowControlled) {
        this.flowControlled = flowControlled;
    }
}
