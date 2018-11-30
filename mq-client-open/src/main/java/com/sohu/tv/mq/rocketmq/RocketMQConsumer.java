package com.sohu.tv.mq.rocketmq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import com.sohu.index.tv.mq.common.BatchConsumerExecutor;
import com.sohu.index.tv.mq.common.ConsumerCallback;
import com.sohu.index.tv.mq.common.ConsumerExecutor;
import com.sohu.tv.mq.common.AbstractConfig;

/**
 * rocketmq 消费者
 * 
 * @Description: push封装
 * @author copy from indexmq
 * @date 2018年1月17日
 */
public class RocketMQConsumer extends AbstractConfig {

    private ConsumerExecutor consumerExecutor;

    // 支持一批消息消费
    private BatchConsumerExecutor batchConsumerExecutor;

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
     * 消费一批消息，最大数
     */
    private int consumeMessageBatchMaxSize = 1;

    /**
     * 是否debug
     */
    private boolean debug;
    
    // "tag1 || tag2 || tag3"
    private String subExpression = "*";

    /**
     * 一个应用创建一个Consumer，由应用来维护此对象，可以设置为全局对象或者单例<br>
     * ConsumerGroupName需要由应用来保证唯一
     */
    public RocketMQConsumer(String consumerGroup, String topic) {
        super(consumerGroup, topic);
        consumer = new DefaultMQPushConsumer(consumerGroup);
    }
    
    public void start() {
        try {
            // 初始化配置
            initConfig(consumer);
            if (getClusterInfoDTO().isBroadcast()) {
                consumer.setMessageModel(MessageModel.BROADCASTING);
            }
            consumer.setConsumeMessageBatchMaxSize(consumeMessageBatchMaxSize);
            consumer.subscribe(topic, subExpression);
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @SuppressWarnings("unchecked")
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                        ConsumeConcurrentlyContext context) {
                    if (msgs == null || msgs.isEmpty()) {
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    List<Map<String, Object>> msgList = null;
                    for (MessageExt me : msgs) {
                        byte[] bytes = me.getBody();
                        try {
                            if (bytes == null || bytes.length == 0) {
                                logger.warn("MessageExt={},MessageBody is null", me);
                                continue;
                            }
                            if (consumerExecutor != null) {
                                Map<String, Object> messageMap = (Map<String, Object>) getMessageSerializer().deserialize(bytes);
                                if (debug) {
                                    logger.warn("messageMap={}, messageExt={}", messageMap, me);
                                }
                                consumerExecutor.execute(messageMap);
                            } else if (consumerCallback != null) {
                                if(getMessageSerializer() == null) {
                                    consumerCallback.call(bytes, me);
                                } else {
                                    Object msgObj = getMessageSerializer().deserialize(bytes);
                                    if (debug) {
                                        logger.warn("messageObj={}, messageExt={}", msgObj, me);
                                    }
                                    consumerCallback.call(msgObj, me);
                                }
                            } else {
                                if (msgList == null) {
                                    msgList = new ArrayList<Map<String, Object>>(msgs.size());
                                }
                                msgList.add((Map<String, Object>) getMessageSerializer().deserialize(bytes));
                            }
                        } catch (Exception e) {
                            logger.error("topic:{} consumer:{} msg:{} msgId:{} bornTimestamp:{}", getTopic(), 
                                    getConsumer(), new String(bytes), me.getMsgId(), me.getBornTimestamp(), e);
                            if (reconsume) {
                                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                            }
                        }
                    }
                    // 一批消费
                    if (batchConsumerExecutor != null) {
                        try {
                            batchConsumerExecutor.execute(msgList);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            if (reconsume) {
                                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                            }
                        }
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
            logger.info("topic:{} group:{} start", topic, group);
        } catch (MQClientException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void shutdown() {
        consumer.shutdown();
    }

    public void setConsumeMessageBatchMaxSize(int consumeMessageBatchMaxSize) {
        this.consumeMessageBatchMaxSize = consumeMessageBatchMaxSize;
    }

    public void setConsumeFromWhere(ConsumeFromWhere consumeFromWhere) {
        consumer.setConsumeFromWhere(consumeFromWhere);
    }

    public void setReconsume(boolean reconsume) {
        this.reconsume = reconsume;
    }

    public void setConsumerExecutor(ConsumerExecutor consumerExecutor) {
        this.consumerExecutor = consumerExecutor;
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
     * 拉取消息的时间间隔，毫秒，默认为0
     * 
     * @param pullInterval
     */
    public void setPullInterval(int pullInterval) {
        consumer.setPullInterval(pullInterval);
    }

    public ConsumerExecutor getConsumerExecutor() {
        return consumerExecutor;
    }

    @SuppressWarnings("rawtypes")
    public ConsumerCallback getConsumerCallback() {
        return consumerCallback;
    }

    public BatchConsumerExecutor getBatchConsumerExecutor() {
        return batchConsumerExecutor;
    }

    public void setBatchConsumerExecutor(BatchConsumerExecutor batchConsumerExecutor) {
        this.batchConsumerExecutor = batchConsumerExecutor;
    }
    
    /**
     * 1.8.3之后不用设置broadcast了，可以自动区分
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

    @Override
    protected int role() {
        return CONSUMER;
    }
}
