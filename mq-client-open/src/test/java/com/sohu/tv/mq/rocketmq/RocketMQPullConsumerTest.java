package com.sohu.tv.mq.rocketmq;

import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: yongfeigao
 * @date: 2022/5/30 11:11
 */
public class RocketMQPullConsumerTest {

    RocketMQPullConsumer pullConsumer;

    @Before
    public void init() {
        pullConsumer = new RocketMQPullConsumer("mqcloud-json-test-bd-consumer", "mqcloud-json-test-topic");
        pullConsumer.setMqCloudDomain("mq-test.com:8080");
        pullConsumer.start();
    }

    @Test
    public void testPull() throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        MessageQueue mq = new MessageQueue();
        mq.setTopic("mqcloud-json-test-topic");
        mq.setBrokerName("broker-b");
        mq.setQueueId(0);
        PullResult pullResult = pullConsumer.getConsumer().pull(mq, "*", 0, 32);
        if (PullStatus.FOUND == pullResult.getPullStatus()) {
            Assert.assertEquals(32, pullResult.getMsgFoundList().size());
        }
        Thread.sleep(10 * 60 * 1000);
    }

    @After
    public void close() {
        pullConsumer.shutdown();
    }
}