package com.sohu.tv.mq.rocketmq.consumer;

import org.apache.rocketmq.common.message.MessageExt;

import com.sohu.tv.mq.rocketmq.RocketMQConsumer;

/**
 * 单个消息消费
 * 
 * @author yongfeigao
 * @date 2021年8月31日
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class SingleMessageConsumer<T> extends AbstractMessageConsumer<T, Void> {

    public SingleMessageConsumer(RocketMQConsumer rocketMQConsumer) {
        super(rocketMQConsumer);
    }

    @Override
    public void consume(T message, MessageExt msgExt) throws Exception {
        rocketMQConsumer.getConsumerCallback().call(message, msgExt);
    }
}
