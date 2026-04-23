package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.ConsumeStatsExt;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.util.MQProtocol;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sohu.tv.mq.util.Constant.BROADCAST;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConsumerServiceTest {
    
    @Autowired
    private ConsumerService consumerService;
    
    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TopicService topicService;

    @Test
    public void test() {
        Topic topic = new Topic();
        topic.setClusterId(clusterService.getMQClusterById(2).getId());
        topic.setName("core-sync-cache-topic");
        List<Consumer> consumerList = new ArrayList<Consumer>();
        Consumer consumer = new Consumer();
        consumer.setId(1);
        consumer.setName("api-dubbo-consumer");
        consumer.setConsumeWay(BROADCAST);
        consumerList.add(consumer);
        Map<Long, List<ConsumeStatsExt>> map = consumerService.fetchBroadcastingConsumeProgress(clusterService.getMQClusterById(2), topic.getName(), consumerList);
        System.out.println(map);
    }

    @Test
    public void resetOffset() {
        consumerService.resetOffset(clusterService.getMQClusterById(3), "mqcloud-json-test-topic", "mqcloud-json-test-consumer2", System.currentTimeMillis());
    }

    @Test
    public void testGetConsumerRunningInfo() {
        Cluster cluster = clusterService.getMQClusterById(8);
        Consumer consumer = new Consumer();
        consumer.setName("mqcloud5-test-consumer");
        Map<String, ConsumerRunningInfo> map = consumerService.getConsumerRunningInfo(cluster, consumer);
        Assert.assertNotNull(map);
    }

    @Test
    public void testCreateConsumer() {
        Topic topic = topicService.queryTopic("mqcloud-json-test-topic").getResult();
        Consumer consumer = new Consumer();
        consumer.setName("mqcloud-json-test-consumer2");
        consumer.setTid(topic.getId());
        Cluster cluster = clusterService.getMQClusterById(topic.getClusterId());
        Result result = consumerService.createConsumer(cluster, consumer, null);
        Assert.assertTrue(result.isOK());
    }

    @Test
    public void testCreateAndUpdateConsumerOnCluster() {
        Consumer consumer = new Consumer();
        consumer.setName("compatible-proxy-remoting-test-consumer");
        consumer.setProtocol(MQProtocol.PROXY_REMOTING.getType());
        consumerService.createAndUpdateConsumerOnCluster(clusterService.getMQClusterById(8), consumer);
    }

    @Test
    public void testDeleteConsumer() {
        consumerService.deleteUnusedAutoSubscribeConsumer();
    }
}
