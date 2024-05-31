package com.sohu.tv.mq.flink.common.config;

import com.sohu.tv.mq.rocketmq.RocketMQConsumer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author fengwang219475
 * @version 1.0
 * @project rocketmq-flink-sohu
 * @description 消费配置
 * @date 2023/4/20 19:15:29
 */
public class FlinkSourceConsumerConfig implements Serializable {

    /**
     * 主题
     */
    private String topic;

    /**
     * 消费者
     */
    private String consumerGroup;

    /**
     * 消费时间戳
     */
    private String consumeTimestamp;

    /**
     * 当消费位点不存在时，从哪开始消费
     */
    private ConsumeFromWhere consumeFromWhere;

    /**
     * 订阅表达式
     */
    private String subExpression;

    /**
     * 批量拉取大小
     */
    private Integer pullBatchSize;

    /**
     * ns 路由域名
     */
    private String mqCloudDomain;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 从指定时间对应的消费位点开始消费
     */
    private Long consumeFromTimestampWhenBoot;

    /**
     * 属性赋值
     * @param consumer
     */
    public void attributeAssignment(RocketMQConsumer consumer){
        Optional.ofNullable(this.consumeTimestamp).ifPresent(consumer::setConsumeTimestamp);
        Optional.ofNullable(this.consumeFromWhere).ifPresent(consumer::setConsumeFromWhere);
        Optional.ofNullable(this.subExpression).ifPresent(consumer::setSubExpression);
        Optional.ofNullable(this.pullBatchSize).ifPresent(consumer::setPullBatchSize);
        Optional.ofNullable(this.mqCloudDomain).ifPresent(consumer::setMqCloudDomain);
        Optional.ofNullable(this.instanceName).ifPresent(consumer::setInstanceName);
        Optional.ofNullable(this.consumeFromTimestampWhenBoot).ifPresent(consumer::setConsumeFromTimestampWhenBoot);
    }

    public FlinkSourceConsumerConfig() {
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getConsumeTimestamp() {
        return consumeTimestamp;
    }

    public void setConsumeTimestamp(String consumeTimestamp) {
        this.consumeTimestamp = consumeTimestamp;
    }

    public ConsumeFromWhere getConsumeFromWhere() {
        return consumeFromWhere;
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        this.consumeFromWhere = consumeFromWhere;
    }

    public String getSubExpression() {
        return subExpression;
    }

    public void setSubExpression(String subExpression) {
        this.subExpression = subExpression;
    }

    public Integer getPullBatchSize() {
        return pullBatchSize;
    }

    public void setPullBatchSize(Integer pullBatchSize) {
        this.pullBatchSize = pullBatchSize;
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

    public Long getConsumeFromTimestampWhenBoot() {
        return consumeFromTimestampWhenBoot;
    }

    public void setConsumeFromTimestampWhenBoot(Long consumeFromTimestampWhenBoot) {
        this.consumeFromTimestampWhenBoot = consumeFromTimestampWhenBoot;
    }
}
