package com.sohu.tv.mq.rocketmq;

import com.sohu.index.tv.mq.common.ConsumerCallback;
import com.sohu.index.tv.mq.common.MQMessage;
import com.sohu.index.tv.mq.common.Result;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

public class MultiLmqTest {

    public static final int TOPIC_COUNT = 100000;
    public static final String PARENT_TOPIC = "multi-lmq-test-topic";
    public static final String LITE_TOPIC_PREFIX = "liteTopic";
    public static final String MSG_PREFIX = "msg";

    @Test
    public void produceToMultiTopic() {
        long startTime = System.currentTimeMillis();
        RocketMQProducer producer = TestUtil.buildProducer(PARENT_TOPIC + "-producer", PARENT_TOPIC);
        producer.start();
        // 发送10万个消息，每个消息包含10个lite topic
        for (int i = 0; i < TOPIC_COUNT; ++i) {
            MQMessage message = MQMessage.build(MSG_PREFIX + i);
            final int index = i;
            IntStream.range(0, 10).forEach(j -> message.addLiteTopic(buildLiteTopic(LITE_TOPIC_PREFIX, index, j)));
            Result<SendResult> sendResult = producer.send(message);
            Assert.assertTrue(sendResult.isSuccess());
        }
        producer.shutdown();
        long endTime = System.currentTimeMillis();
        System.out.println("Produced " + TOPIC_COUNT + " messages in " + (endTime - startTime) + " ms");
    }

    @Test
    public void consumeSomeTopic() throws InterruptedException {
        // 消费10个lite topic，验证消息是否正确
        int consumeTopicCount = 10;
        int base = TOPIC_COUNT / consumeTopicCount;
        CountDownLatch latch = new CountDownLatch(consumeTopicCount);
        for (int i = 0; i < consumeTopicCount; ++i) {
            int index = i * base;
            String subTopic = buildLiteTopic(LITE_TOPIC_PREFIX, index, 0);
            RocketMQConsumer consumer = TestUtil.buildLMQConsumer(subTopic + "-consumer", PARENT_TOPIC, subTopic);
            consumer.setConsumerCallback((ConsumerCallback<String, MessageExt>) (t, k) -> {
                String expectedMsg = MSG_PREFIX + index;
                if (expectedMsg.equals(t)) {
                    latch.countDown();
                } else {
                    Assert.fail("Expected message: " + expectedMsg + ", but got: " + t);
                }
            });
            consumer.start();
        }
        latch.await();
    }

    private String buildLiteTopic(String topic, int index, int subIndex) {
        return topic + "-" + index + "-" + subIndex;
    }
}
