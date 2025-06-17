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
     * 设置clientId
     *
     * @param clientId
     */
    public void setClientId(String clientId);

    /**
     * 暂停消费
     */
    void pause();

    /**
     * 恢复消费
     */
    void resume();

    /**
     * 消费状态
     * 
     * @author yongfeigao
     * @date 2021年8月31日
     */
    public class ConsumeStatus {
        private boolean ok;
        private Throwable exception;
        public static final ConsumeStatus OK = new ConsumeStatus(true);

        private ConsumeStatus() {
        }

        private ConsumeStatus(boolean ok) {
            this.ok = ok;
        }

        public static ConsumeStatus fail(Throwable e) {
            ConsumeStatus status = new ConsumeStatus();
            status.exception = e;
            return status;
        }

        public boolean isOk() {
            return ok;
        }

        public boolean isFail() {
            return !ok;
        }

        public Throwable getException() {
            return exception;
        }
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
