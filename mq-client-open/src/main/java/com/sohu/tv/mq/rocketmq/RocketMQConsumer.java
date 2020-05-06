package com.sohu.tv.mq.rocketmq;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.client.trace.hook.ConsumeMessageTraceHookImpl;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.common.utils.HttpTinyClient;
import org.apache.rocketmq.common.utils.HttpTinyClient.HttpResult;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sohu.index.tv.mq.common.BatchConsumerCallback;
import com.sohu.index.tv.mq.common.ConsumerCallback;
import com.sohu.tv.mq.common.AbstractConfig;
import com.sohu.tv.mq.dto.DTOResult;
import com.sohu.tv.mq.dto.MessageResetDTO;

/**
 * rocketmq 消费者
 * 
 * @Description: push封装
 * @author copy from indexmq
 * @date 2018年1月17日
 */
@SuppressWarnings("deprecation")
public class RocketMQConsumer extends AbstractConfig {

    // 支持一批消息消费
    private BatchConsumerCallback<?, MessageExt> batchConsumerCallback;

    /**
     * 消费者
     */
    private DefaultMQPushConsumer consumer;

    @SuppressWarnings("rawtypes")
    private ConsumerCallback consumerCallback;

    /**
     * 是否重试
     */
    private boolean reconsume = true;

    /**
     * 是否debug
     */
    private boolean debug;

    // "tag1 || tag2 || tag3"
    private String subExpression = "*";

    // 是否顺序消费
    private boolean consumeOrderly = false;

    // 跳过重试消息时间，默认为-1，即不跳过
    private volatile long retryMessageResetTo = -1;

    /**
     * 一个应用创建一个Consumer，由应用来维护此对象，可以设置为全局对象或者单例<br>
     * ConsumerGroupName需要由应用来保证唯一
     */
    public RocketMQConsumer(String consumerGroup, String topic) {
        super(consumerGroup, topic);
        consumer = new DefaultMQPushConsumer(consumerGroup);
        // 消费消息超时将会发回重试队列，超时时间由默认的15分钟修改为2小时
        consumer.setConsumeTimeout(2 * 60);
    }

    public void start() {
        try {
            // 初始化配置
            initConfig(consumer);
            if (getClusterInfoDTO().isBroadcast()) {
                consumer.setMessageModel(MessageModel.BROADCASTING);
            }
            consumer.subscribe(topic, subExpression);

            // 构建消费者对象
            final MessageConsumer messageConsumer = new MessageConsumer(this);
            // 注册顺序或并发消费
            if (consumeOrderly) {
                consumer.registerMessageListener(new MessageListenerOrderly() {
                    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                        return messageConsumer.consumeMessage(msgs, context);
                    }
                });
            } else {
                consumer.registerMessageListener(new MessageListenerConcurrently() {
                    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                            ConsumeConcurrentlyContext context) {
                        return messageConsumer.consumeMessage(msgs, context);
                    }
                });
            }
            // 初始化定时调度任务
            initScheduleTask();
            // 消费者启动
            consumer.start();
            logger.info("topic:{} group:{} start", topic, group);
        } catch (MQClientException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 从mqcloud更新重试消息跳过消息的时间
     */
    private void initScheduleTask() {
        // 数据采样线程
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "skipRetryMessageTimeThread-" + getGroup());
            }
        }).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpResult result = HttpTinyClient.httpGet(
                            "http://" + getMqCloudDomain() + "/consumer/reset/" + getGroup(), null, null, "UTF-8", 5000);
                    if (HttpURLConnection.HTTP_OK != result.code) {
                        logger.error("http response err: code:{},info:{}", result.code, result.content);
                        return;
                    }
                    DTOResult<MessageResetDTO> dtoResult = JSON.parseObject(result.content, new TypeReference<DTOResult<MessageResetDTO>>(){});
                    MessageResetDTO messageResetDTO = dtoResult.getResult();
                    if(messageResetDTO == null) {
                        return;
                    }
                    if (messageResetDTO.getResetTo() != retryMessageResetTo) {
                        logger.info("retryMessage reset from:{} to:{}", retryMessageResetTo,
                                messageResetDTO.getResetTo());
                        retryMessageResetTo = messageResetDTO.getResetTo();
                    }
                } catch (Throwable ignored) {
                    logger.warn("skipRetryMessage err:{}", ignored);
                }
            }
        }, 60, 300, TimeUnit.SECONDS);
    }

    public void shutdown() {
        consumer.shutdown();
    }

    /**
     * Batch consumption size
     * 
     * @param consumeMessageBatchMaxSize
     */
    public void setConsumeMessageBatchMaxSize(int consumeMessageBatchMaxSize) {
        if (consumeMessageBatchMaxSize <= 0) {
            return;
        }
        consumer.setConsumeMessageBatchMaxSize(consumeMessageBatchMaxSize);
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        consumer.setConsumeFromWhere(consumeFromWhere);
    }

    public void setReconsume(boolean reconsume) {
        this.reconsume = reconsume;
    }

    @SuppressWarnings("rawtypes")
    public void setConsumerCallback(ConsumerCallback consumerCallback) {
        this.consumerCallback = consumerCallback;
    }

    public void setConsumeTimestamp(String consumeTimestamp) {
        consumer.setConsumeTimestamp(consumeTimestamp);
    }

    public DefaultMQPushConsumer getConsumer() {
        return consumer;
    }

    /**
     * 消费线程数，默认20
     * 
     * @param num
     */
    public void setConsumeThreadMin(int num) {
        if (num <= 0) {
            return;
        }
        consumer.setConsumeThreadMin(num);
    }

    /**
     * 消费线程数，默认64
     * 
     * @param num
     */
    public void setConsumeThreadMax(int num) {
        if (num <= 0) {
            return;
        }
        consumer.setConsumeThreadMax(num);
    }

    /**
     * 一次拉取多少个消息 ，默认32
     * 
     * @param size
     */
    public void setPullBatchSize(int size) {
        if (size < 0) {
            return;
        }
        consumer.setPullBatchSize(size);
    }

    /**
     * queue中缓存多少个消息时进行流控 ，默认1000
     * 
     * @param size
     */
    public void setPullThresholdForQueue(int size) {
        if (size < 0) {
            return;
        }
        consumer.setPullThresholdForQueue(size);
    }

    /**
     * queue中缓存多少M消息时进行流控 ，默认100
     * 
     * @param size
     */
    public void setPullThresholdSizeForQueue(int size) {
        if (size < 0) {
            return;
        }
        consumer.setPullThresholdSizeForQueue(size);
    }

    /**
     * topic维度缓存多少个消息时进行流控 ，默认-1，不限制
     * 
     * @param size
     */
    public void setPullThresholdForTopic(int size) {
        if (size < 0) {
            return;
        }
        consumer.setPullThresholdForTopic(size);
    }

    /**
     * topic维度缓存多少M消息时进行流控 ，默认-1，不限制
     * 
     * @param size
     */
    public void setPullThresholdSizeForTopic(int size) {
        if (size < 0) {
            return;
        }
        consumer.setPullThresholdSizeForTopic(size);
    }

    /**
     * 拉取消息的时间间隔，毫秒，默认为0
     * 
     * @param pullInterval
     */
    public void setPullInterval(int pullInterval) {
        consumer.setPullInterval(pullInterval);
    }

    @SuppressWarnings("rawtypes")
    public ConsumerCallback getConsumerCallback() {
        return consumerCallback;
    }

    @SuppressWarnings("unchecked")
    public <T> BatchConsumerCallback<T, MessageExt> getBatchConsumerCallback() {
        return (BatchConsumerCallback<T, MessageExt>) batchConsumerCallback;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setBatchConsumerCallback(BatchConsumerCallback batchConsumerCallback) {
        this.batchConsumerCallback = batchConsumerCallback;
    }

    /**
     * 1.8.3之后不用设置broadcast了，可以自动区分
     * 
     * @param broadcast
     */
    @Deprecated
    public void setBroadcast(boolean broadcast) {
    }

    public String getSubExpression() {
        return subExpression;
    }

    public void setSubExpression(String subExpression) {
        this.subExpression = subExpression;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isReconsume() {
        return reconsume;
    }

    public void setConsumeOrderly(boolean consumeOrderly) {
        this.consumeOrderly = consumeOrderly;
    }

    @Override
    protected int role() {
        return CONSUMER;
    }

    @Override
    protected void registerTraceDispatcher(AsyncTraceDispatcher traceDispatcher) {
        consumer.getDefaultMQPushConsumerImpl().registerConsumeMessageHook(
                new ConsumeMessageTraceHookImpl(traceDispatcher));
    }

    /**
     * traceEnabled is controlled by MQCloud
     * 
     * @param traceEnabled
     */
    @Deprecated
    public void setTraceEnabled(boolean traceEnabled) {
    }

    /**
     * Maximum amount of time in minutes a message may block the consuming
     * thread.
     */
    public void setConsumeTimeout(long consumeTimeout) {
        if (consumeTimeout <= 0) {
            return;
        }
        consumer.setConsumeTimeout(consumeTimeout);
    }

    /**
     * 是否开启vip通道
     * 
     * @param vipChannelEnabled
     */
    public void setVipChannelEnabled(boolean vipChannelEnabled) {
        consumer.setVipChannelEnabled(vipChannelEnabled);
    }

    public long getRetryMessageResetTo() {
        return retryMessageResetTo;
    }

    public void setRetryMessageResetTo(long retryMessageResetTo) {
        this.retryMessageResetTo = retryMessageResetTo;
    }
    
    /**
     * 最大重新消费次数
     * 默认为16次
     * @param maxReconsumeTimes
     */
    public void setMaxReconsumeTimes(int maxReconsumeTimes) {
        consumer.setMaxReconsumeTimes(maxReconsumeTimes);
    }
}
