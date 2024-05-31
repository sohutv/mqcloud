package com.sohu.tv.mq.flink.common.config;

import com.sohu.tv.mq.rocketmq.RocketMQProducer;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author fengwang219475
 * @version 1.0
 * @project rocketmq-flink-sohu
 * @description
 * @date 2023/4/20 19:22:46
 */
public class FlinkSinkProducerConfig implements Serializable {

    /**
     * 主题
     */
    private String topic;

    /**
     * 生产者
     */
    private String producerGroup;

    /**
     * 超时时间
     */
    private Integer sendMsgTimeout;

    /**
     * ns 路由域名
     */
    private String mqCloudDomain;

    /**
     * 实例唯一名称
     */
    private String instanceName;

    /**
     * 重试次数，区别与rocketmq的默认重试，此为业务重试指定
     */
    private int retryCount = 0;

    /**
     * 赋值属性
     * @param producer
     */
    public void attributeAssignment(RocketMQProducer producer){
        Optional.ofNullable(sendMsgTimeout).ifPresent(producer::setSendMsgTimeout);
        Optional.ofNullable(mqCloudDomain).ifPresent(producer::setMqCloudDomain);
        Optional.ofNullable(instanceName).ifPresent(producer::setInstanceName);
    }

    public FlinkSinkProducerConfig() {
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getProducerGroup() {
        return producerGroup;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public Integer getSendMsgTimeout() {
        return sendMsgTimeout;
    }

    public void setSendMsgTimeout(Integer sendMsgTimeout) {
        this.sendMsgTimeout = sendMsgTimeout;
    }

    public String getMqCloudDomain() {
        return mqCloudDomain;
    }

    public void setMqCloudDomain(String mqCloudDomain) {
        this.mqCloudDomain = mqCloudDomain;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
