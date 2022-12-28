package com.sohu.tv.mq.rocketmq;

import com.sohu.index.tv.mq.common.ConsumerCallback;
import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.rocketmq.RocketMQProducerJsonTest.Video;
import com.sohu.tv.mq.util.CommonUtil;
import com.sohu.tv.mq.util.JSONUtil;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: yongfeigao
 * @date: 2022/11/18 14:47
 */
public class AffinityTest {

    private AtomicLong counter = new AtomicLong();

    @Test
    public void produce() throws Exception {
        RocketMQProducer producer = TestUtil.buildProducer("mqcloud-json-test-topic-producer", "mqcloud-json-test-topic");
        producer.setAffinityEnabled(true);
        producer.setAffinityBrokerSuffix(CommonUtil.MQ_AFFINITY_DEFAULT);
        producer.start();
        for(int i = 0; i < 10000; ++i) {
            Video video = new Video(i, "搜狐tv"+i);
            String str = JSONUtil.toJSONString(video);
            Result<SendResult> sendResult = producer.publish(str, String.valueOf(i));
            System.out.println(sendResult);
            Assert.assertTrue(sendResult.isSuccess());
            Thread.sleep(1000);
        }
    }

    @Test
    public void produceTx() throws Exception {
        RocketMQProducer producer = TestUtil.buildProducer("mqcloud-json-test-topic-producer", "mqcloud-json-test-topic");
        producer.setAffinityEnabled(true);
        producer.setAffinityBrokerSuffix("tx");
        producer.start();
        for(int i = 0; i < 10000; ++i) {
            Video video = new Video(i, "搜狐tv"+i);
            String str = JSONUtil.toJSONString(video);
            Result<SendResult> sendResult = producer.publish(str, String.valueOf(i));
            System.out.println(sendResult);
            Assert.assertTrue(sendResult.isSuccess());
            Thread.sleep(1000);
        }
    }

    @Test
    public void consume() throws InterruptedException {
        RocketMQConsumer consumer = TestUtil.buildConsumer("mqcloud-json-test-consumer", "mqcloud-json-test-topic");
        consumer.setAffinityEnabled(true);
        consumer.setAffinityBrokerSuffix(CommonUtil.MQ_AFFINITY_DEFAULT);
        consumer.setConsumerCallback(new ConsumerCallback<String, MessageExt>() {
            public void call(String t, MessageExt k) throws Exception {
                if (counter.incrementAndGet() % 10 == 0) {
                }
            }
        });
        consumer.start();
        while (true) {
            System.out.println(counter.get());
            Thread.sleep(10000);
        }
    }

    @Test
    public void consumeTx() throws InterruptedException {
        RocketMQConsumer consumer = TestUtil.buildConsumer("mqcloud-json-test-consumer", "mqcloud-json-test-topic");
        consumer.setAffinityEnabled(true);
        consumer.setAffinityBrokerSuffix("tx");
        consumer.setConsumerCallback(new ConsumerCallback<String, MessageExt>() {
            public void call(String t, MessageExt k) throws Exception {
                if (counter.incrementAndGet() % 10 == 0) {
                }
            }
        });
        consumer.start();
        while (true) {
            System.out.println(counter.get());
            Thread.sleep(10000);
        }
    }

    @Test
    public void consumeTx2() throws InterruptedException {
        RocketMQConsumer consumer = TestUtil.buildConsumer("mqcloud-json-test-consumer", "mqcloud-json-test-topic");
        consumer.setAffinityEnabled(true);
        consumer.setAffinityBrokerSuffix("tx");
        consumer.setConsumerCallback(new ConsumerCallback<String, MessageExt>() {
            public void call(String t, MessageExt k) throws Exception {
                if (counter.incrementAndGet() % 10 == 0) {
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
