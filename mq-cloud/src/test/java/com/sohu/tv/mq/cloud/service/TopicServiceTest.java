package com.sohu.tv.mq.cloud.service;

import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.AuditTopic;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicServiceTest {
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    @Autowired
    private TopicService topicService;
    
    @Autowired
    private ClusterService clusterService;
    
    @Test
    public void testRoute() throws Exception {
        String topic = "enterprise_media_topic";
        String[] array = {"127.0.0.1:9876"};
        for(String addr : array) {
            DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt("tewtaetatta");
            defaultMQAdminExt.setVipChannelEnabled(false);
            defaultMQAdminExt.setNamesrvAddr(addr);
            defaultMQAdminExt.start();
            TopicRouteData route = defaultMQAdminExt.examineTopicRouteInfo(topic);
            System.out.println(addr);
            System.out.println(route);
            System.out.println("====");
            defaultMQAdminExt.shutdown();
        }
    }

    @Test
    public void test() {
        Cluster mqCluster = clusterService.getMQClusterById(2);
        String topic = "core-sync-cache-topic";
        
        mqAdminTemplate.execute(new DefaultCallback<TopicRouteData>() {
            public TopicRouteData callback(MQAdminExt mqAdmin) throws Exception {
                TopicRouteData t = mqAdmin.examineTopicRouteInfo(topic);
                return t;
            }
            @Override
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }

    @Test
    public void testDelete() {
        Cluster mqCluster = clusterService.getMQClusterById(2);
        String topic = "starfans-news_message_online-topic";
        topicService.deleteTopicOnCluster(mqCluster, topic);
    }
    
    @Test
    public void testCreate() {
        Cluster mqCluster = clusterService.getMQClusterById(1);
        String topic = "starfans-news_message_online-topic";
        AuditTopic at = new AuditTopic();
        at.setName(topic);
        at.setQueueNum(8);
        topicService.createAndUpdateTopicOnCluster(mqCluster, at);
    }

    @Test
    public void testCreateOrderTopic(){
        Cluster mqCluster = clusterService.getMQClusterById(8);
        String topic = "mqcloud-order-topic";
        AuditTopic at = new AuditTopic();
        at.setName(topic);
        at.setQueueNum(8);
        at.setOrdered(1);
        topicService.createAndUpdateTopicOnCluster(mqCluster, at);
    }
}
