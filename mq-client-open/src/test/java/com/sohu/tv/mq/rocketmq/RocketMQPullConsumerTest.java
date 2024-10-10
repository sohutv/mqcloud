package com.sohu.tv.mq.rocketmq;

import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

/**
 * @author: yongfeigao
 * @date: 2022/5/30 11:11
 */
public class RocketMQPullConsumerTest {

    RocketMQPullConsumer pullConsumer;

    @Before
    public void init() {
        pullConsumer = new RocketMQPullConsumer("mqcloud-json-test-consumer", "mqcloud-json-test-topic");
        pullConsumer.setMqCloudDomain(TestUtil.MQ_CLOUD_IP + ":8080");
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

    @Test
    public void testGetTopicStatsTable() {
        Set<String> brokers = pullConsumer.findBrokerAddressInSubscribe();
        if (CollectionUtils.isEmpty(brokers)) {
            return;
        }
        for (String broker : brokers) {
            TopicStatsTable topicStatsTableTmp = pullConsumer.getTopicStatsTable(broker);
            Assert.assertNotNull(topicStatsTableTmp);
        }
    }

    @After
    public void close() {
        pullConsumer.shutdown();
    }
}