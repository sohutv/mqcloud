package com.sohu.tv.mq.rocketmq;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.rocketmq.common.message.MessageExt;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sohu.index.tv.mq.common.BatchConsumerCallback;
import com.sohu.index.tv.mq.common.ConsumerCallback;
import com.sohu.index.tv.mq.common.MQMessage;

public class RocketMQConsumerJsonTest {

    private AtomicLong counter = new AtomicLong();

    private RocketMQConsumer consumer;

    @Before
    public void init() {
        consumer = TestUtil.buildConsumer("mqcloud-json-test-consumer", "mqcloud-json-test-topic");
    }

    @Test
    public void test() throws InterruptedException {
        consumer.setConsumerCallback(new ConsumerCallback<String, MessageExt>() {
            public void call(String t, MessageExt k) throws Exception {
                if (counter.incrementAndGet() % 10 == 0) {
                    System.out.println(t);
                }
            }
        });
        consumer.start();
        while (true) {
            System.out.println(counter.get());
            Thread.sleep(1000);
        }
    }
    
    @Test
    public void testBatch() throws InterruptedException {
        consumer.setConsumeMessageBatchMaxSize(32);
        consumer.setBatchConsumerCallback(new BatchConsumerCallback<String>(){
            public void call(List<MQMessage<String>> batchMessage) throws Exception {
                for(MQMessage<String> mqMessage : batchMessage) {
                    counter.incrementAndGet();
                    System.out.println("msg:" + mqMessage.getMessage() + ",msgExt:" + mqMessage.getMessageExt());
                }
            }
        });
        consumer.start();
        while (true) {
            System.out.println(counter.get());
            Thread.sleep(10000);
        }
    }

    @After
    public void clean() {
        consumer.shutdown();
    }
}
