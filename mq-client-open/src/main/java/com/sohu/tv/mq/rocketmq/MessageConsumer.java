package com.sohu.tv.mq.rocketmq;

import java.util.ArrayList;
import java.util.List;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;

import com.sohu.index.tv.mq.common.BatchConsumerCallback.MQMessage;
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
    
    public MessageConsumer(RocketMQConsumer rocketMQConsumer) {
        this.rocketMQConsumer = rocketMQConsumer;
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
            rocketMQConsumer.getMessageConsumeRateLimiter().acquire();
        } catch (InterruptedException e) {
            rocketMQConsumer.getLogger().warn("acquirePermit error", e.getMessage());
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
                    rocketMQConsumer.getLogger().error("consume topic:{} consumer:{} msg:{} msgId:{} bornTimestamp:{}",
                            rocketMQConsumer.getTopic(), rocketMQConsumer.getConsumer(), mqMessage.getMessage(),
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
        @SuppressWarnings("unchecked")
        protected List<MQMessage<T, MessageExt>> parse(List<MessageExt> msgs){
            if (msgs == null || msgs.isEmpty()) {
                return null;
            }
            List<MQMessage<T, MessageExt>> msgList = new ArrayList<>(msgs.size());
            for (MessageExt me : msgs) {
                byte[] bytes = me.getBody();
                try {
                    if (bytes == null || bytes.length == 0) {
                        rocketMQConsumer.getLogger().warn("MessageExt={}, body is null", me);
                        continue;
                    }
                    // 校验是否需要跳过重试消息
                    if(CommonUtil.isRetryTopic(me.getProperty(MessageConst.PROPERTY_REAL_TOPIC)) && 
                            me.getBornTimestamp() < rocketMQConsumer.getRetryMessageResetTo()) {
                        rocketMQConsumer.getLogger().warn("skip topic:{} msgId:{} bornTime:{}", 
                                rocketMQConsumer.getTopic(), me.getMsgId(), me.getBornTimestamp());
                        continue;
                    }
                    if (rocketMQConsumer.getMessageSerializer() == null) {
                        msgList.add((MQMessage<T, MessageExt>) new MQMessage<>(bytes, me));
                    } else {
                        msgList.add((MQMessage<T, MessageExt>) new MQMessage<>(
                                rocketMQConsumer.getMessageSerializer().deserialize(bytes), me));
                    }
                } catch (Throwable e) {
                    // 解析失败打印警告，不再抛出异常重试(即使重试，仍然会失败)
                    rocketMQConsumer.getLogger().error("parse topic:{} consumer:{} msg:{} msgId:{} bornTimestamp:{}", 
                            rocketMQConsumer.getTopic(), rocketMQConsumer.getConsumer(), new String(bytes), me.getMsgId(), 
                            me.getBornTimestamp(), e);
                }
            }
            return msgList;
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
                rocketMQConsumer.getLogger().error("topic:{} consumer:{} msgSize:{}", 
                        rocketMQConsumer.getTopic(), rocketMQConsumer.getConsumer(), msgList.size(), e);
                return ConsumeStatus.FAIL;
            }
            return ConsumeStatus.OK;
        }

        @Override
        public void consume(Object message, MessageExt msgExt) throws Exception {
        }
    }
}
