package com.sohu.tv.mq.rocketmq;

import java.util.ArrayList;
import java.util.List;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;

import com.alibaba.fastjson.JSON;
import com.sohu.index.tv.mq.common.BatchConsumerCallback.MQMessage;
import com.sohu.tv.mq.serializable.MessageSerializer;
import com.sohu.tv.mq.serializable.MessageSerializerEnum;
import com.sohu.tv.mq.util.CommonUtil;
/**
 * 消息消费公共逻辑抽取
 * 
 * @author yongfeigao
 * @date 2019年1月22日
 */
public class MessageConsumer {
    
    private RocketMQConsumer rocketMQConsumer;
    
    // 消费策略
    private ConsumerStrategy consumerStrategy;
    
    private Logger logger;
    
    public MessageConsumer(RocketMQConsumer rocketMQConsumer) {
        this.rocketMQConsumer = rocketMQConsumer;
        logger = rocketMQConsumer.getLogger();
        consumerStrategy = new ConsumerStrategy();
    }

    /**
     * 消费并发消息
     * @param msgs
     * @param context
     * @return
     */
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        ConsumeStatus consumeStatus = consumerStrategy.chooseConsumer().consume(msgs);
        if (ConsumeStatus.FAIL == consumeStatus && rocketMQConsumer.isReconsume()) {
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
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
        ConsumeStatus consumeStatus = consumerStrategy.chooseConsumer().consume(msgs);
        if (ConsumeStatus.FAIL == consumeStatus && rocketMQConsumer.isReconsume()) {
            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
        return ConsumeOrderlyStatus.SUCCESS;
    }
    
    /**
     * 获取许可
     */
    private void acquirePermit() {
        try {
            rocketMQConsumer.getRateLimiter().limit();
        } catch (InterruptedException e) {
            logger.warn("acquirePermit error", e.getMessage());
        }
    }
    
    /**
     * 消费状态
     * 
     * @author yongfeigao
     * @date 2019年1月22日
     */
    private enum ConsumeStatus {
        OK,
        FAIL,
        ;
    }
    
    /**
     * 消费者
     * 
     * @author yongfeigao
     * @date 2019年10月21日
     */
    private interface IConsumer {
        public ConsumeStatus consume(List<MessageExt> msgs);
    }
    
    /**
     * 消费逻辑抽象
     * 
     * @author yongfeigao
     * @date 2019年10月21日
     * @param <T>
     */
    private abstract class AbstractConsumer <T> implements IConsumer {
        
        /**
         * 消费逻辑
         */
        public ConsumeStatus consume(List<MessageExt> msgs) {
            // 解析消息
            List<MQMessage<T, MessageExt>> messageList = parse(msgs);
            if (messageList == null || messageList.isEmpty()) {
                return ConsumeStatus.OK;
            }
            // 消费消息
            for (MQMessage<T, MessageExt> mqMessage : messageList) {
                try {
                    // 获取许可
                    acquirePermit();
                    consume(mqMessage.getMessage(), mqMessage.getMessageExt());
                } catch (Throwable e) {
                    logger.error("consume topic:{} consumer:{} msgId:{} bornTimestamp:{}",
                            rocketMQConsumer.getTopic(), rocketMQConsumer.getGroup(),
                            mqMessage.getMessageExt().getMsgId(), mqMessage.getMessageExt().getBornTimestamp(), e);
                    return ConsumeStatus.FAIL;
                }
            }
            return ConsumeStatus.OK;
        }
        
        /**
         * 解析消息
         * @param msgs
         * @return
         */
        protected List<MQMessage<T, MessageExt>> parse(List<MessageExt> msgs){
            if (msgs == null || msgs.isEmpty()) {
                return null;
            }
            List<MQMessage<T, MessageExt>> msgList = new ArrayList<>(msgs.size());
            for (MessageExt me : msgs) {
                byte[] bytes = me.getBody();
                try {
                    if (bytes == null || bytes.length == 0) {
                        logger.warn("MessageExt={}, body is null", me);
                        continue;
                    }
                    // 校验是否需要跳过重试消息
                    if(CommonUtil.isRetryTopic(me.getProperty(MessageConst.PROPERTY_REAL_TOPIC)) && 
                            me.getBornTimestamp() < rocketMQConsumer.getRetryMessageResetTo()) {
                        logger.warn("skip topic:{} msgId:{} bornTime:{}", 
                                rocketMQConsumer.getTopic(), me.getMsgId(), me.getBornTimestamp());
                        continue;
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
        
        @SuppressWarnings("unchecked")
        private MQMessage<T, MessageExt> buildMQMessage(MessageExt me) throws Exception {
            byte[] bytes = me.getBody();
            // 无序列化器直接返回
            if (rocketMQConsumer.getMessageSerializer() == null) {
                debugLog("null-serializer", me.getMsgId(), bytes.getClass().getName(), null);
                return (MQMessage<T, MessageExt>) new MQMessage<>(bytes, me);
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
                return (MQMessage<T, MessageExt>) new MQMessage<>(JSON.toJSONString(message), me);
            }
            // 消息为String，采用JSON转换
            if (message instanceof String) {
                debugLog("String-Message", me.getMsgId(), "String", consumerParameterTypeClass.getName());
                return (MQMessage<T, MessageExt>) new MQMessage<>(
                        JSON.parseObject(message.toString(), consumerParameterTypeClass), me);
            }
            debugLog("unknown", me.getMsgId(), message.getClass().getName(), consumerParameterTypeClass.getName());
            // 消费类型和消息都不是String，并且消息与消费类型不匹配，此时可能会类转换异常
            return (MQMessage<T, MessageExt>) new MQMessage<>(message, me);
        }
        
        private void debugLog(String flag, String msgId, String msgType, String consumerType) {
            logger.debug("detectType:{} consumer:{} msgId:{} {}->{}", flag, rocketMQConsumer.getGroup(), msgId, msgType,
                    consumerType);
        }
        
        /**
         * 反序列化，若设置的反序列化器执行失败，则使用其他反序列化器进行尝试
         * @param bytes
         * @param me
         * @return
         * @throws Exception
         */
        @SuppressWarnings("unchecked")
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
         * @param message
         * @param msgExt
         * @throws Exception
         */
        public abstract void consume(T message, MessageExt msgExt) throws Exception;
    }
    
    /**
     * 消费者选择策略
     * 
     * @author yongfeigao
     * @date 2019年10月21日
     */
    private class ConsumerStrategy {
        
        @SuppressWarnings("rawtypes")
        private _ConsumerCallback _consumerCallback = new _ConsumerCallback();
        
        private _BatchConsumerCallback _batchConsumerCallback = new _BatchConsumerCallback();
        
        /**
         * 挑选消费者
         * 优先选择ConsumerCallback
         * 其次选择BatchConsumerCallback
         * @return
         */
        public IConsumer chooseConsumer() {
            if (rocketMQConsumer.getConsumerCallback() != null) {
                return _consumerCallback;
            }
            return _batchConsumerCallback;
        }
    }
    
    /**
     * for @ConsumerCallback
     * 
     * @author yongfeigao
     * @date 2019年10月21日
     */
    private class _ConsumerCallback<T> extends AbstractConsumer<T> {
        
        @Override
        @SuppressWarnings("unchecked")
        public void consume(T message, MessageExt msgExt) throws Exception {
            rocketMQConsumer.getConsumerCallback().call(message, msgExt);            
        }
        
    }
    
    /**
     * for @BatchConsumerCallback
     * 
     * @author yongfeigao
     * @date 2019年10月21日
     */
    private class _BatchConsumerCallback extends AbstractConsumer<Object> {

        public ConsumeStatus consume(List<MessageExt> msgs) {
            List<MQMessage<Object, MessageExt>> msgList = parse(msgs);
            if (msgList == null || msgList.isEmpty()) {
                return ConsumeStatus.OK;
            }
            try {
                rocketMQConsumer.getBatchConsumerCallback().call(msgList);
            } catch (Throwable e) {
                logger.error("topic:{} consumer:{} msgSize:{}", 
                        rocketMQConsumer.getTopic(), rocketMQConsumer.getGroup(), msgList.size(), e);
                return ConsumeStatus.FAIL;
            }
            return ConsumeStatus.OK;
        }

        @Override
        public void consume(Object message, MessageExt msgExt) throws Exception {
        }
    }
}
