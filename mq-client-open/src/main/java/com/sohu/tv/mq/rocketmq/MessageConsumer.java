package com.sohu.tv.mq.rocketmq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
/**
 * 消息消费公共逻辑抽取
 * 
 * @author yongfeigao
 * @date 2019年1月22日
 */
public class MessageConsumer {
    
    private RocketMQConsumer rocketMQConsumer;
    
    public MessageConsumer(RocketMQConsumer rocketMQConsumer) {
        super();
        this.rocketMQConsumer = rocketMQConsumer;
    }

    /**
     * 消费并发消息
     * @param msgs
     * @param context
     * @return
     */
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        ConsumeStatus consumeStatus = consumeMessage(msgs);
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
        ConsumeStatus consumeStatus = consumeMessage(msgs);
        if (ConsumeStatus.FAIL == consumeStatus && rocketMQConsumer.isReconsume()) {
            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
        return ConsumeOrderlyStatus.SUCCESS;
    }
    
    
    /**
     * 消费消息
     * @param msgs
     * @return
     */
    @SuppressWarnings("unchecked")
    private ConsumeStatus consumeMessage(List<MessageExt> msgs) {
        if (msgs == null || msgs.isEmpty()) {
            return ConsumeStatus.OK;
        }
        List<Map<String, Object>> msgList = null;
        for (MessageExt me : msgs) {
            byte[] bytes = me.getBody();
            try {
                if (bytes == null || bytes.length == 0) {
                    rocketMQConsumer.getLogger().warn("MessageExt={},MessageBody is null", me);
                    continue;
                }
                if (rocketMQConsumer.getConsumerExecutor() != null) {
                    Map<String, Object> messageMap = (Map<String, Object>) rocketMQConsumer.getMessageSerializer().deserialize(bytes);
                    if (rocketMQConsumer.isDebug()) {
                        rocketMQConsumer.getLogger().warn("messageMap={}, messageExt={}", messageMap, me);
                    }
                    rocketMQConsumer.getConsumerExecutor().execute(messageMap);
                } else if (rocketMQConsumer.getConsumerCallback() != null) {
                    if(rocketMQConsumer.getMessageSerializer() == null) {
                        rocketMQConsumer.getConsumerCallback().call(bytes, me);
                    } else {
                        Object msgObj = rocketMQConsumer.getMessageSerializer().deserialize(bytes);
                        if (rocketMQConsumer.isDebug()) {
                            rocketMQConsumer.getLogger().warn("messageObj={}, messageExt={}", msgObj, me);
                        }
                        rocketMQConsumer.getConsumerCallback().call(msgObj, me);
                    }
                } else {
                    if (msgList == null) {
                        msgList = new ArrayList<Map<String, Object>>(msgs.size());
                    }
                    msgList.add((Map<String, Object>) rocketMQConsumer.getMessageSerializer().deserialize(bytes));
                }
            } catch (Exception e) {
                rocketMQConsumer.getLogger().error("topic:{} consumer:{} msg:{} msgId:{} bornTimestamp:{}", 
                        rocketMQConsumer.getTopic(), rocketMQConsumer.getConsumer(), new String(bytes), me.getMsgId(), 
                        me.getBornTimestamp(), e);
                return ConsumeStatus.FAIL;
            }
        }
        // 一批消费
        if (rocketMQConsumer.getBatchConsumerExecutor() != null) {
            try {
                rocketMQConsumer.getBatchConsumerExecutor().execute(msgList);
            } catch (Exception e) {
                rocketMQConsumer.getLogger().error(e.getMessage(), e);
                return ConsumeStatus.FAIL;
            }
        }
        return ConsumeStatus.OK;
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
}
