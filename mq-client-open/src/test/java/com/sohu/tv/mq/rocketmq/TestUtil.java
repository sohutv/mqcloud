package com.sohu.tv.mq.rocketmq;

import com.sohu.tv.mq.common.AbstractConfig;
import com.sohu.tv.mq.rocketmq.redis.RedisBuilder;
import com.sohu.tv.mq.serializable.StringSerializer;
import org.apache.rocketmq.client.producer.TransactionListener;

public class TestUtil {

    public static RocketMQConsumer buildConsumer(String consumerGroup, String topic) {
        RocketMQConsumer consumer = new RocketMQConsumer(consumerGroup, topic);
        setDomain(consumer);
        return buildConsumer(consumerGroup, topic, false);
    }

    public static RocketMQConsumer buildConsumer(String consumerGroup, String topic, boolean deduplicate) {
        RocketMQConsumer consumer = new RocketMQConsumer(consumerGroup, topic);
        setDomain(consumer);
        if (deduplicate) {
            consumer.setRedis(RedisBuilder.build("127.0.0.1", 6379));
        }
        return consumer;
    }

    public static RocketMQProducer buildProducer(String producerGroup, String topic) {
        RocketMQProducer producer = new RocketMQProducer(producerGroup, topic);
        setDomain(producer);
        return producer;
    }

    public static RocketMQProducer buildProducer(String producerGroup, String topic,
            TransactionListener transactionListener) {
        RocketMQProducer producer = new RocketMQProducer(producerGroup, topic, transactionListener);
        setDomain(producer);
        return producer;
    }

    private static void setDomain(AbstractConfig abstractConfig) {
        abstractConfig.setMqCloudDomain("127.0.0.1:8080");
        abstractConfig.setMessageSerializer(new StringSerializer<>());
    }
}
