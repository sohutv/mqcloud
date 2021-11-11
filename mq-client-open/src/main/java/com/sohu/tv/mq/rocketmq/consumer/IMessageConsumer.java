package com.sohu.tv.mq.rocketmq.consumer;

import java.util.List;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 消息消费
 * 
 * @author yongfeigao
 * @date 2021年8月31日
 * @param <C>
 */
public interface IMessageConsumer<C> {

    /**
     * 消费并发消息
     * @param msgs
     * @param context
     * @return
     */
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context);
    
    /**
     * 消费顺序消息
     * @param msgs
     * @param context
     * @return
     */
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context);

    /**
     * 消费状态
     * 
     * @author yongfeigao
     * @date 2021年8月31日
     */
    public enum ConsumeStatus {
        OK, 
        FAIL,
        ;
    }

    /**
     * 消费上下文
     * 
     * @author yongfeigao
     * @date 2021年8月31日
     * @param <C>
     */
    public class MessageContext<C> {
        List<MessageExt> msgs;
        C context;

        public MessageContext(List<MessageExt> msgs, C context) {
            this.msgs = msgs;
            this.context = context;
        }

        public MessageContext(List<MessageExt> msgs) {
            this.msgs = msgs;
        }

        public List<MessageExt> getMsgs() {
            return msgs;
        }

        public C getContext() {
            return context;
        }
    }
}
