package com.sohu.tv.mq.rocketmq;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.rocketmq.common.message.MessageClientExt;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.ConsumerCallback;

public class RocketMQDeduplicateConsumerTest {
    private AtomicLong counter = new AtomicLong();

    private RocketMQConsumer consumer;

    @Before
    public void init() {
        consumer = TestUtil.buildConsumer("basic-apitest-topic-consumer", "basic-apitest-topic", true);
    }

    @Test
    public void test() throws InterruptedException {
        consumer.setDeduplicateWindowSeconds(10 * 60);
        consumer.setConsumerCallback(new ConsumerCallback<Map<String, Object>, MessageExt>() {
            public void call(Map<String, Object> t, MessageExt k) {
                System.out.println("consume msgId:"+k.getMsgId()+",offsetMsgId:"+((MessageClientExt)k).getOffsetMsgId());
                try {
                    Thread.sleep(120 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        consumer.start();
        while (true) {
            Thread.sleep(10000);
        }
    }
    
    @Test
    public void testRetry() throws InterruptedException {
        consumer.setConsumerCallback(new ConsumerCallback<Map<String, Object>, MessageExt>() {
            public void call(Map<String, Object> t, MessageExt k) {
                if (counter.incrementAndGet() == 1) {
                    throw new RuntimeException("test");
                }
            }
        });
        consumer.start();
        while (true) {
            System.out.println(counter.get());
            Thread.sleep(10000);
        }
    }
}
