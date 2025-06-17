package com.sohu.tv.mq.rocketmq.consumer;

import com.sohu.index.tv.mq.common.MQMessage;
import com.sohu.tv.mq.metric.*;
import com.sohu.tv.mq.rocketmq.RocketMQConsumer;
import com.sohu.tv.mq.serializable.MessageSerializer;
import com.sohu.tv.mq.serializable.MessageSerializerEnum;
import com.sohu.tv.mq.stats.ConsumeStats;
import com.sohu.tv.mq.util.CommonUtil;
import com.sohu.tv.mq.util.JSONUtil;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

/**
 * 公共逻辑
 * 
 * @author yongfeigao
 * @date 2021年8月31日
 * @param <T>
 * @param <C>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class AbstractMessageConsumer<T, C> implements IMessageConsumer<C> {

    protected Logger logger;

    protected RocketMQConsumer rocketMQConsumer;
    
    // 消费统计
    protected ConsumeStats consumeStats;

    protected Set<Thread> pausedThreads = ConcurrentHashMap.newKeySet();

    public AbstractMessageConsumer(RocketMQConsumer rocketMQConsumer) {
        this.rocketMQConsumer = rocketMQConsumer;
        this.logger = rocketMQConsumer.getLogger();
        if (rocketMQConsumer.isEnableStats()) {
            consumeStats = new ConsumeStats(rocketMQConsumer.getGroup());
            consumeStats.setMqcloudDomain(rocketMQConsumer.getMqCloudDomain());
            MQMetricsExporter.getInstance().add(consumeStats);
        }
    }
    
    /**
     * 消费并发消息
     * @param msgs
     * @param context
     * @return
     */
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        long start = System.currentTimeMillis();
        ConsumeStatus consumeStatus = consume(new MessageContext(msgs, context));
        if (consumeStatus.isFail() && rocketMQConsumer.isReconsume()) {
            if (consumeStats != null) {
                consumeStats.incrementException(consumeStatus.getException());
            }
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        if (consumeStats != null) {
            consumeStats.increment(System.currentTimeMillis() - start);
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
    
    /**
     * 消费顺序消息
     * @param msgs
     * @param context
     * @return
     */
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        long start = System.currentTimeMillis();
        ConsumeStatus consumeStatus = consume(new MessageContext(msgs, context));
        if (consumeStatus.isFail() && rocketMQConsumer.isReconsume()) {
            if (consumeStats != null) {
                consumeStats.incrementException(consumeStatus.getException());
            }
            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
        if (consumeStats != null) {
            consumeStats.increment(System.currentTimeMillis() - start);
        }
        return ConsumeOrderlyStatus.SUCCESS;
    }

    /**
     * 消费逻辑
     */
    public ConsumeStatus consume(MessageContext<C> context) {
        // 解析消息
        List<MQMessage<T>> messageList = parse(context.msgs);
        if (messageList == null || messageList.isEmpty()) {
            return ConsumeStatus.OK;
        }
        // 设置消费线程统计
        String group = rocketMQConsumer.getGroup();
        ConsumeThreadStat metric = ConsumeStatManager.getInstance().getConsumeThreadMetrics(group);
        try {
            metric.set(buildThreadConsumeMetric(messageList));
            if (rocketMQConsumer.isPause()) {
                pause();
            }
            // 消费消息
            for (MQMessage<T> mqMessage : messageList) {
                try {
                    // 获取限速许可
                    acquirePermit();
                    consume(mqMessage.getMessage(), mqMessage.getMessageExt());
                } catch (Throwable e) {
                    logger.error("consume topic:{} consumer:{} msgId:{} bornTimestamp:{}",
                            rocketMQConsumer.getTopic(), group, mqMessage.getMsgId(),
                            mqMessage.getMessageExt().getBornTimestamp(), e);
                    ConsumeStatManager.getInstance().getConsumeFailedMetrics(group)
                            .set(buildMessageExceptionMetric(mqMessage, e));
                    return ConsumeStatus.fail(e);
                }
            }
        } finally {
            metric.remove();
        }
        return ConsumeStatus.OK;
    }

    public void pause() {
        Thread currentThread = Thread.currentThread();
        pausedThreads.add(currentThread);
        LockSupport.park();
        pausedThreads.remove(currentThread);
    }

    public void resume() {
        pausedThreads.forEach(LockSupport::unpark);
    }

    /**
     * 解析消息
     * 
     * @param msgs
     * @return
     */
    protected List<MQMessage<T>> parse(List<MessageExt> msgs) {
        if (msgs == null || msgs.isEmpty()) {
            return null;
        }
        List<MQMessage<T>> msgList = new ArrayList<>(msgs.size());
        for (MessageExt me : msgs) {
            byte[] bytes = me.getBody();
            try {
                if (bytes == null || bytes.length == 0) {
                    logger.warn("MessageExt={}, body is null", me);
                    continue;
                }
                // 校验是否需要跳过重试消息
                if (!CommonUtil.isDeadTopic(me.getTopic()) && CommonUtil.isRetryTopic(me.getProperty(MessageConst.PROPERTY_REAL_TOPIC)) &&
                        me.getBornTimestamp() < rocketMQConsumer.getRetryMessageResetTo()) {
                    if (rocketMQConsumer.getRetryMessageSkipKey() != null) {
                        if (rocketMQConsumer.getRetryMessageSkipKey().equals(me.getKeys())) {
                            logger.warn("skip topic:{} msgId:{} bornTime:{} key:{}",
                                    rocketMQConsumer.getTopic(), me.getMsgId(), me.getBornTimestamp(),
                                    me.getKeys());
                            continue;
                        }
                    } else {
                        logger.warn("skip topic:{} msgId:{} bornTime:{}",
                                rocketMQConsumer.getTopic(), me.getMsgId(), me.getBornTimestamp());
                        continue;
                    }
                }
                msgList.add(buildMQMessage(me));
            } catch (Throwable e) {
                // 解析失败打印警告，不再抛出异常重试(即使重试，仍然会失败)
                logger.error("parse topic:{} consumer:{} msg:{} msgId:{} bornTimestamp:{}",
                        rocketMQConsumer.getTopic(), rocketMQConsumer.getGroup(), new String(bytes), me.getMsgId(),
                        me.getBornTimestamp(), e);
            }
        }
        return msgList;
    }

    private MQMessage<T> buildMQMessage(MessageExt me) throws Exception {
        byte[] bytes = me.getBody();
        // 无序列化器直接返回
        if (rocketMQConsumer.getMessageSerializer() == null) {
            debugLog("null-serializer", me.getMsgId(), bytes.getClass().getName(), null);
            return (MQMessage<T>) new MQMessage<>(bytes, me);
        }
        // 反序列化
        T message = deserialize(me);
        Class<?> consumerParameterTypeClass = rocketMQConsumer.getConsumerParameterTypeClass();
        // 无法获取消费类型
        if (consumerParameterTypeClass == null) {
            debugLog("null-consumerParameterType", me.getMsgId(), message.getClass().getName(), null);
            return new MQMessage<>(message, me);
        }
        // 反序列化后类型相同直接返回
        if (consumerParameterTypeClass.isInstance(message)) {
            debugLog("isInstance", me.getMsgId(), message.getClass().getName(), consumerParameterTypeClass.getName());
            return new MQMessage<>(message, me);
        }
        // 消费类型为String，采用JSON转换
        if (consumerParameterTypeClass == String.class) {
            debugLog("String-consumerParameterType", me.getMsgId(), message.getClass().getName(), "String");
            return (MQMessage<T>) new MQMessage<>(JSONUtil.toJSONString(message), me);
        }
        // 消息为String，采用JSON转换
        if (message instanceof String) {
            debugLog("String-Message", me.getMsgId(), "String", consumerParameterTypeClass.getName());
            return (MQMessage<T>) new MQMessage<>(JSONUtil.parse(message.toString(), consumerParameterTypeClass), me);
        }
        debugLog("unknown", me.getMsgId(), message.getClass().getName(), consumerParameterTypeClass.getName());
        // 消费类型和消息都不是String，并且消息与消费类型不匹配，此时可能会类转换异常
        return (MQMessage<T>) new MQMessage<>(message, me);
    }

    private void debugLog(String flag, String msgId, String msgType, String consumerType) {
        logger.debug("detectType:{} consumer:{} msgId:{} {}->{}", flag, rocketMQConsumer.getGroup(), msgId, msgType,
                consumerType);
    }

    /**
     * 反序列化，若设置的反序列化器执行失败，则使用其他反序列化器进行尝试
     * 
     * @param bytes
     * @param me
     * @return
     * @throws Exception
     */
    private T deserialize(MessageExt me) throws Exception {
        // 使用设置的序列化器
        MessageSerializer<Object> messageSerializer = rocketMQConsumer.getMessageSerializer();
        Exception excp = null;
        try {
            T t = (T) messageSerializer.deserialize(me.getBody());
            if (logger.isDebugEnabled()) {
                logger.debug("consumer:{} msgId:{} deserializer:{}",
                        rocketMQConsumer.getGroup(), me.getMsgId(), messageSerializer.getClass().getName());
            }
            return t;
        } catch (Exception e) {
            excp = e;
        }
        // 使用其他序列化器
        for (MessageSerializerEnum messageSerializerEnum : MessageSerializerEnum.values()) {
            if (messageSerializer.getClass() == messageSerializerEnum.getMessageSerializer().getClass()) {
                continue;
            }
            try {
                T t = (T) messageSerializerEnum.getMessageSerializer().deserialize(me.getBody());
                if (logger.isDebugEnabled()) {
                    logger.debug("consumer:{} msgId:{} compatible deserializer:{}",
                            rocketMQConsumer.getGroup(), me.getMsgId(),
                            messageSerializerEnum.getMessageSerializer().getClass().getName());
                }
                return t;
            } catch (Exception e) {
                logger.warn("try deserializer:{}, topic:{}, consumer:{} msgId:{} err",
                        messageSerializerEnum.getMessageSerializer().getClass().getName(),
                        rocketMQConsumer.getTopic(), rocketMQConsumer.getGroup(), me.getMsgId());
            }
        }
        throw excp;
    }

    /**
     * 具体消费逻辑
     * 
     * @param message
     * @param msgExt
     * @throws Exception
     */
    public abstract void consume(T message, MessageExt msgExt) throws Exception;

    /**
     * 构建线程消费统计
     * 
     * @param messageList
     */
    protected MessageMetric buildThreadConsumeMetric(List<MQMessage<T>> messageList) {
        List<String> idList = new ArrayList<>(messageList.size());
        messageList.forEach(message -> {
            idList.add(message.buildOffsetMsgId());
        });
        MessageMetric messageMetric = new MessageMetric();
        messageMetric.setStartTime(System.currentTimeMillis());
        messageMetric.setMsgIdList(idList);
        return messageMetric;
    }

    /**
     * 构建异常消费统计
     * 
     * @param messageList
     */
    protected MessageExceptionMetric buildMessageExceptionMetric(List<MQMessage<T>> messageList, Throwable e) {
        List<String> idList = new ArrayList<>(messageList.size());
        messageList.forEach(message -> {
            idList.add(message.buildOffsetMsgId());
        });
        MessageExceptionMetric messageMetric = buildMessageExceptionMetric(e);
        messageMetric.setMsgIdList(idList);
        return messageMetric;
    }

    /**
     * 构建异常消费统计
     * 
     * @param messageList
     */
    protected MessageExceptionMetric buildMessageExceptionMetric(MQMessage<T> mqMessage, Throwable e) {
        List<String> idList = new ArrayList<>(1);
        idList.add(mqMessage.buildOffsetMsgId());
        MessageExceptionMetric messageMetric = buildMessageExceptionMetric(e);
        messageMetric.setMsgIdList(idList);
        return messageMetric;
    }

    protected MessageExceptionMetric buildMessageExceptionMetric(Throwable e) {
        MessageExceptionMetric messageMetric = new MessageExceptionMetric();
        messageMetric.setStartTime(System.currentTimeMillis());
        messageMetric.setThreadId(Thread.currentThread().getId());
        messageMetric.setThreadName(Thread.currentThread().getName());
        messageMetric.setException(e);
        return messageMetric;
    }

    /**
     * 获取许可
     */
    protected void acquirePermit() {
        try {
            rocketMQConsumer.getRateLimiter().limit();
        } catch (InterruptedException e) {
            logger.warn("acquirePermit error", e.getMessage());
        }
    }

    /**
     * 获取许可
     */
    protected void acquirePermit(int permits) {
        try {
            rocketMQConsumer.getRateLimiter().limit(permits);
        } catch (InterruptedException e) {
            logger.warn("acquirePermit error", e.getMessage());
        }
    }

    @Override
    public void setClientId(String clientId) {
        if (consumeStats != null) {
            consumeStats.setClientId(clientId);
        }
    }
}
