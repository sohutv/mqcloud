package com.sohu.tv.mq.rocketmq.producer;

import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.MessageQueue;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.LongAdder;

public class RendezvousHashMessageQueueSelectorTest {
    @Test
    public void testSelect() {
        int brokerCount = 5;
        int queueCount = 8;
        List<MessageQueue> messageQueues = buildMessageQueues(brokerCount, queueCount);
        RendezvousHashMessageQueueSelector selector = new RendezvousHashMessageQueueSelector();
        List<MessageQueue> messageQueuesCopy = new ArrayList<>(messageQueues);
        for (int i = 0; i < 10000; ++i) {
            String key = UUID.randomUUID().toString();
            MessageQueue selectedMessageQueue = selector.select(messageQueues, null, key);
            for (MessageQueue mq : messageQueues) {
                messageQueuesCopy.remove(mq);
                if (!selectedMessageQueue.equals(mq)) {
                    Assert.assertEquals(selectedMessageQueue, selector.select(messageQueuesCopy, null, key));
                }
                messageQueuesCopy.add(mq);
                Assert.assertEquals(selectedMessageQueue, selector.select(messageQueues, null, key));
            }
        }
    }

    @Test
    public void testMigrateRate() {
        int brokerCount = 5;
        int queueCount = 8;
        List<MessageQueue> messageQueues = buildMessageQueues(brokerCount, queueCount);
        List<MessageQueue> messageQueuesCopy = new ArrayList<>(messageQueues);
        messageQueuesCopy.remove(0);
        MessageQueueSelector rendezvousSelector = new RendezvousHashMessageQueueSelector();
        MessageQueueSelector simpleSelector = new SelectMessageQueueByHash();

        int rendezvousNotEqualCount = 0;
        int simpleNotEqualCount = 0;
        int totalKeys = 100000;
        for (int i = 0; i < totalKeys; ++i) {
            String key = UUID.randomUUID().toString();
            MessageQueue selectedMessageQueue = rendezvousSelector.select(messageQueues, null, key);
            MessageQueue selectedMessageQueue2 = rendezvousSelector.select(messageQueuesCopy, null, key);
            if (!selectedMessageQueue.equals(selectedMessageQueue2)) {
                rendezvousNotEqualCount++;
            }
            MessageQueue selectedMessageQueue3 = simpleSelector.select(messageQueues, null, key);
            MessageQueue selectedMessageQueue4 = simpleSelector.select(messageQueuesCopy, null, key);
            if (!selectedMessageQueue3.equals(selectedMessageQueue4)) {
                simpleNotEqualCount++;
            }
        }
        System.out.println("Rendezvous迁移比例：" + rate(rendezvousNotEqualCount, totalKeys) + "%");
        System.out.println("Simple迁移比例：" + rate(simpleNotEqualCount, totalKeys) + "%");
    }

    @Test
    public void testDistributed() {
        int brokerCount = 5;
        int queueCount = 8;
        int totalQueueCount = brokerCount * queueCount;
        List<MessageQueue> messageQueues = buildMessageQueues(brokerCount, queueCount);
        HashMap<String, LongAdder> map = new HashMap<>();
        long times = 100000;
        RendezvousHashMessageQueueSelector selector = new RendezvousHashMessageQueueSelector();
        String prefix = "video";
        for (int i = 0; i < times; i++) {
            String key = UUID.randomUUID().toString();
            MessageQueue mq = selector.select(messageQueues, null, prefix + key);
            String mqKey = mq.getBrokerName() + "-" + mq.getQueueId();
            map.computeIfAbsent(mqKey, k -> new LongAdder()).increment();
        }
        double avg = times / totalQueueCount;
        map.forEach((k, v) -> {
            System.out.println(k + "与平均值差比" + minusRate(v.longValue(), avg) + "%");
        });
        System.out.println("标准差：" + standardDeviation(map) + ",平均值：" + times / totalQueueCount);
    }

    private List<MessageQueue> buildMessageQueues(int brokerCount, int queueCount) {
        List<MessageQueue> messageQueues = new ArrayList<>();
        String topic = "test-topic";
        for (int i = 0; i < brokerCount; ++i) {
            String brokerName = "broker-" + i;
            for (int j = 0; j < queueCount; ++j) {
                MessageQueue messageQueue = new MessageQueue(topic, brokerName, j);
                messageQueues.add(messageQueue);
            }
        }
        return messageQueues;
    }

    private double minusRate(double n, double n2) {
        return rate(Math.abs(n - n2), n2);
    }

    private double rate(double n, double n2) {
        double rate = n / n2;
        return (long) (rate * 100) / 100D * 100;
    }

    public double standardDeviation(Map<String, LongAdder> map) {
        int size = map.size();
        // 求和
        long sum = map.values().stream().mapToLong(LongAdder::longValue).sum();
        // 求平均值
        double dAve = sum / size;
        double dVar = 0;
        // 求方差
        for (Object key : map.keySet()) {
            long value = map.get(key).longValue();
            dVar += (value - dAve) * (value - dAve);
        }
        return Math.sqrt(dVar / size);
    }
}