package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ConsumeStatsExt;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConsumerServiceTest {
    
    @Autowired
    private ConsumerService consumerService;
    
    @Autowired
    private ClusterService clusterService;

    @Test
    public void test() {
        Topic topic = new Topic();
        topic.setClusterId(clusterService.getMQClusterById(2).getId());
        topic.setName("core-sync-cache-topic");
        List<Consumer> consumerList = new ArrayList<Consumer>();
        Consumer consumer = new Consumer();
        consumer.setId(1);
        consumer.setName("api-dubbo-consumer");
        consumer.setConsumeWay(Consumer.BROADCAST);
        consumerList.add(consumer);
        Map<Long, List<ConsumeStatsExt>> map = consumerService.fetchBroadcastConsumeProgress(topic, consumerList);
        System.out.println(map);
    }

    @Test
    public void resetOffset() {
        consumerService.resetOffset(clusterService.getMQClusterById(1), "%RETRY%jellyfish-group-preonline", "jellyfish-group-preonline", 10*1000);
    }
}
