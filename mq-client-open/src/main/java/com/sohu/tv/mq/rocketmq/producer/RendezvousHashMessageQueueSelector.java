package com.sohu.tv.mq.rocketmq.producer;

import com.google.common.hash.Hashing;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Rendezvous Hash MessageQueueSelector
 *
 * @author yongfeigao
 * @date 2026年04月02日
 */
public class RendezvousHashMessageQueueSelector implements MessageQueueSelector {

    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        String key = String.valueOf(arg);
        long maxScore = Long.MIN_VALUE;
        MessageQueue selectedQueue = null;
        for (MessageQueue mq : mqs) {
            long score = hash(key, mq);
            if (score > maxScore) {
                maxScore = score;
                selectedQueue = mq;
            }
        }
        return selectedQueue;
    }

    private long hash(String key, MessageQueue mq) {
        return Hashing.murmur3_128().newHasher()
                .putString(key, StandardCharsets.UTF_8)
                .putString(mq.getBrokerName(), StandardCharsets.UTF_8)
                .putInt(mq.getQueueId())
                .hash()
                .asLong();
    }
}
