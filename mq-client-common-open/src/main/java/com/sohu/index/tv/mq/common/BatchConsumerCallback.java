package com.sohu.index.tv.mq.common;

import java.util.List;

/**
 * 批量消费回调
 * @Description: 
 * @author yongfeigao
 * @date 2018年1月19日
 * @param <T> msg obj
 * @param MessageExt
 */
public interface BatchConsumerCallback<T, MessageExt> {

    /**
     * 订阅回调方法
     *
     * @return
     */
    void call(List<MQMessage<T, MessageExt>> batchMessage) throws Exception;

    /**
     * 批量消息
     * 
     * @author yongfeigao
     * @date 2019年10月18日
     * @param <T>
     * @param <MessageExt>
     */
    public class MQMessage<T, MessageExt> {
        private T message;
        private MessageExt messageExt;

        public MQMessage(T message, MessageExt messageExt) {
            this.message = message;
            this.messageExt = messageExt;
        }

        public T getMessage() {
            return message;
        }

        public void setMessage(T message) {
            this.message = message;
        }

        public MessageExt getMessageExt() {
            return messageExt;
        }

        public void setMessageExt(MessageExt messageExt) {
            this.messageExt = messageExt;
        }
    }
}
