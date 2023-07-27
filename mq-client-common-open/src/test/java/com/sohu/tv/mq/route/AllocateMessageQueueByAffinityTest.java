package com.sohu.tv.mq.route;

import com.sohu.tv.mq.util.CommonUtil;
import org.apache.rocketmq.common.message.MessageQueue;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: yongfeigao
 * @date: 2022/12/2 16:09
 */
public class AllocateMessageQueueByAffinityTest {

    AllocateMessageQueueByAffinity allocateMessageQueueByAffinity = new AllocateMessageQueueByAffinity();
    String consumerGroup = "mqConsumer";

    @BeforeClass
    public static void init() {
    }

    @Test
    public void testNoNewestClient() {
        String currentCID = "127.0.0.1@123@1";
        List<MessageQueue> mqAll = buildDefaultMQ();
        List<String> cidAll = new ArrayList<>();
        cidAll.add(currentCID);
        cidAll.add("127.0.0.1@124@1");
        cidAll.add("127.0.0.1@125@1");
        cidAll.add("127.0.0.1@126@1");
        List<MessageQueue> mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        Assert.assertEquals(6, mqs.size());

        currentCID = "127.0.0.1@124@1";
        mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        Assert.assertEquals(6, mqs.size());

        currentCID = "127.0.0.1@125@1";
        mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        Assert.assertEquals(6, mqs.size());

        currentCID = "127.0.0.1@126@1";
        mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        Assert.assertEquals(6, mqs.size());
    }

    @Test
    public void testNotAllNewestClient() {
        String currentCID = "127.0.0.1@123_" + CommonUtil.MQ_AFFINITY_DEFAULT + "@1";
        List<MessageQueue> mqAll = buildDefaultMQ();
        List<String> cidAll = new ArrayList<>();
        cidAll.add(currentCID);
        cidAll.add("127.0.0.1@124@1");
        cidAll.add("127.0.0.1@125@1");
        cidAll.add("127.0.0.1@126@1");
        List<MessageQueue> mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        Assert.assertEquals(6, mqs.size());
    }

    @Test
    public void testNotAffinityClient() {
        String currentCID = "127.0.0.1@123_" + CommonUtil.MQ_AFFINITY_DEFAULT + "@1";
        List<MessageQueue> mqAll = buildTwoAffinityMQ();
        List<String> cidAll = new ArrayList<>();
        cidAll.add(currentCID);
        List<MessageQueue> mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        Assert.assertEquals(32, mqs.size());
    }

    @Test
    public void testNotAffinityClient2() {
        String currentCID = "127.0.0.1@123_" + CommonUtil.MQ_AFFINITY_DEFAULT + "@1";
        List<MessageQueue> mqAll = buildTwoAffinityMQ();
        List<String> cidAll = new ArrayList<>();
        cidAll.add(currentCID);
        cidAll.add("127.0.0.1@124_" + CommonUtil.MQ_AFFINITY_DEFAULT + "@1");
        List<MessageQueue> mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        Assert.assertEquals(16, mqs.size());
    }

    @Test
    public void testAffinityClient() {
        String currentCID = "127.0.0.1@123_" + CommonUtil.MQ_AFFINITY_DEFAULT + "@1";
        List<MessageQueue> mqAll = buildTwoAffinityMQ();
        List<String> cidAll = new ArrayList<>();
        cidAll.add(currentCID);
        cidAll.add("127.0.0.1@124_tx@1");
        List<MessageQueue> mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        Assert.assertEquals(16, mqs.size());
        checkResult(mqs, 16, CommonUtil.MQ_AFFINITY_DEFAULT);

        currentCID = "127.0.0.1@124_tx@1";
        mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        checkResult(mqs, 16, "tx");
    }

    @Test
    public void testThreeNoAffinityClient() {
        String currentCID = "127.0.0.1@123_" + CommonUtil.MQ_AFFINITY_DEFAULT + "@1";
        List<MessageQueue> mqAll = buildThreeAffinityMQ();
        List<String> cidAll = new ArrayList<>();
        cidAll.add(currentCID);
        cidAll.add("127.0.0.1@124_tx@1");
        List<MessageQueue> mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        Assert.assertEquals(16, mqs.size());

        currentCID = "127.0.0.1@124_tx@1";
        mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        Assert.assertEquals(16, mqs.size());
    }

    @Test
    public void testThreeAffinityClient() {
        String currentCID = "127.0.0.1@123_" + CommonUtil.MQ_AFFINITY_DEFAULT + "@1";
        List<MessageQueue> mqAll = buildThreeAffinityMQ();
        List<String> cidAll = new ArrayList<>();
        cidAll.add(currentCID);
        String currentCID2 = "127.0.0.1@22_" + CommonUtil.MQ_AFFINITY_DEFAULT + "@1";
        cidAll.add(currentCID2);
        cidAll.add("127.0.0.1@124_tx@1");
        cidAll.add("127.0.0.1@124_tx@1");
        cidAll.add("127.0.0.1@125_hw@1");
        List<MessageQueue> mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        checkResult(mqs, 8, CommonUtil.MQ_AFFINITY_DEFAULT);

        currentCID = currentCID2;
        mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        checkResult(mqs, 8 , CommonUtil.MQ_AFFINITY_DEFAULT);

        currentCID = "127.0.0.1@124_tx@1";
        mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        checkResult(mqs, 4, "tx");

        currentCID = "127.0.0.1@125_hw@1";
        mqs = allocateMessageQueueByAffinity.allocate(consumerGroup, currentCID, mqAll, cidAll);
        checkResult(mqs, 8, "hw");
    }

    public void checkResult(List<MessageQueue> mqs, int size, String flag) {
        Assert.assertEquals(size, mqs.size());
        for (MessageQueue messageQueue : mqs) {
            if (CommonUtil.MQ_AFFINITY_DEFAULT.equals(flag)) {
                Assert.assertTrue(!messageQueue.getBrokerName().contains(CommonUtil.MQ_AFFINITY_DELIMITER));
            } else {
                Assert.assertTrue(messageQueue.getBrokerName().endsWith(flag));
            }
        }
    }

    public List<MessageQueue> buildDefaultMQ() {
        List<MessageQueue> mqAll = new ArrayList<>();
        for (char flag = 'a'; flag < 'd'; ++flag) {
            for (int i = 0; i < 8; ++i) {
                MessageQueue messageQueue = new MessageQueue();
                messageQueue.setBrokerName("broker-" + flag);
                messageQueue.setQueueId(i);
                messageQueue.setTopic("topic");
                mqAll.add(messageQueue);
            }
        }
        return mqAll;
    }

    public List<MessageQueue> buildTwoAffinityMQ() {
        List<MessageQueue> mqAll = new ArrayList<>();
        for (char flag = 'a'; flag <= 'd'; ++flag) {
            for (int i = 0; i < 8; ++i) {
                MessageQueue messageQueue = new MessageQueue();
                if (flag == 'a' || flag == 'b') {
                    messageQueue.setBrokerName("broker-" + flag);
                } else {
                    messageQueue.setBrokerName("broker-" + flag + CommonUtil.MQ_AFFINITY_DELIMITER + "tx");
                }
                messageQueue.setQueueId(i);
                messageQueue.setTopic("topic");
                mqAll.add(messageQueue);
            }
        }
        return mqAll;
    }

    public List<MessageQueue> buildThreeAffinityMQ() {
        List<MessageQueue> mqAll = new ArrayList<>();
        for (char flag = 'a'; flag <= 'd'; ++flag) {
            for (int i = 0; i < 8; ++i) {
                MessageQueue messageQueue = new MessageQueue();
                if (flag == 'a') {
                    messageQueue.setBrokerName("broker-" + flag);
                } else if (flag == 'b') {
                    messageQueue.setBrokerName("broker-" + flag + CommonUtil.MQ_AFFINITY_DELIMITER + "tx");
                } else if (flag == 'c') {
                    messageQueue.setBrokerName("broker-" + flag + CommonUtil.MQ_AFFINITY_DELIMITER + "hw");
                } else {
                    messageQueue.setBrokerName("broker-" + flag);
                }
                messageQueue.setQueueId(i);
                messageQueue.setTopic("topic");
                mqAll.add(messageQueue);
            }
        }
        return mqAll;
    }
}