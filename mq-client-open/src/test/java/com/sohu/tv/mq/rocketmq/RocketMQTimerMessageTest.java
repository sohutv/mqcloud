package com.sohu.tv.mq.rocketmq;

import com.sohu.index.tv.mq.common.ConsumerCallback;
import com.sohu.index.tv.mq.common.MQMessage;
import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.rocketmq.RocketMQProducerJsonTest.Video;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

public class RocketMQTimerMessageTest {

    @Test
    public void test() throws InterruptedException {
        String topic = "mqcloud-timer-test-topic";
        String producer = "mqcloud-timer-test-topic-producer";
        RocketMQProducer rocketMQProducer = TestUtil.buildProducer(producer, topic);
        rocketMQProducer.start();

        int msgSize = 100;
        for (int i = 0; i < msgSize; ++i) {
            Video video = new Video(i, "sohu-tv");
            long deliveryTimestamp = System.currentTimeMillis() + 60 * 1000;
            MQMessage<?> mqMessage = MQMessage.build(video).setDeliveryTimestamp(deliveryTimestamp);
            Result<SendResult> sendResult = rocketMQProducer.send(mqMessage);
            Assert.assertTrue(sendResult.isSuccess());
            Thread.sleep(1000);
        }

        String consumer = "mqcloud-timer-test-consumer";
        AtomicLong counter = new AtomicLong();
        RocketMQConsumer rocketMQConsumer = TestUtil.buildConsumer(consumer, topic);
        rocketMQConsumer.setConsumerCallback(new ConsumerCallback<String, MessageExt>() {
            public void call(String t, MessageExt k) throws Exception {
                counter.incrementAndGet();
                System.out.println("msg:" + t + ",msgExt:" + k);
            }
        });
        rocketMQConsumer.start();
        long start = System.currentTimeMillis();
        while (true) {
            long count = counter.get();
            System.out.println(count + ", second:" + (System.currentTimeMillis() - start) / 1000);
            Thread.sleep(1000);
            if (count >= msgSize) {
                rocketMQProducer.shutdown();
                rocketMQConsumer.shutdown();
                break;
            }
        }
    }
}
