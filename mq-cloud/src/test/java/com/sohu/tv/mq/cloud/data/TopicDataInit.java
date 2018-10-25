package com.sohu.tv.mq.cloud.data;

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.mq.DefaultInvoke;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.TopicService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicDataInit {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private TopicService topicService;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    @Autowired
    private ClusterService clusterService;
    
    @Test
    public void test() {
        Cluster mqCluster = clusterService.getMQClusterById(2);
        String topic = "pgc_sync_web_test";
        mqAdminTemplate.execute(new DefaultInvoke() {
            public void invoke(MQAdminExt mqAdmin) throws Exception {
                // 获取broker集群信息
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                // 获取一个master地址
                String brokerAddr = clusterInfo.getBrokerAddrTable().entrySet().iterator().next().getValue().getBrokerAddrs().get(0L);
                // 获取所有topic配置
                TopicConfigSerializeWrapper allTopicConfig = mqAdmin.getAllTopicGroup(brokerAddr, 5000);
                // 获取路由信息
                Topic topicObject = new Topic();
                topicObject.setClusterId(mqCluster.getId());
                topicObject.setName(topic);
                TopicConfig topicConfig = allTopicConfig.getTopicConfigTable().get(topic);
                if(topicConfig != null) {
                    topicObject.setOrdered(topicConfig.isOrder() ? 1 : 0);
                    topicObject.setQueueNum(topicConfig.getReadQueueNums());
                    try {
                        topicService.save(topicObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    logger.error("topic:{} no config", topic);
                }
            }
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }

    @Test
    public void initTopicData() {
        for(Cluster mqCluter : clusterService.getAllMQCluster()) {
            mqAdminTemplate.execute(new DefaultInvoke() {
                public void invoke(MQAdminExt mqAdmin) throws Exception {
                    // 获取broker集群信息
                    ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                    // 获取一个master地址
                    String brokerAddr = clusterInfo.getBrokerAddrTable().entrySet().iterator().next().getValue().getBrokerAddrs().get(0L);
                    // 获取所有topic配置
                    TopicConfigSerializeWrapper allTopicConfig = mqAdmin.getAllTopicGroup(brokerAddr, 5000);
                    // 获取所有topic
                    TopicList topicList = mqAdmin.fetchAllTopicList();
                    for(String topic : topicList.getTopicList()) {
                        if (topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)
                                || topic.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX)
                                || topic.contains("OFFSET_MOVED_EVENT")
                                //|| topic.toLowerCase().contains("test")
                                || topic.startsWith("broker-")
                                || topic.endsWith("-cluster")) {
                            continue;
                        }
                        // 获取路由信息
                        Topic topicObject = new Topic();
                        topicObject.setClusterId(mqCluter.getId());
                        topicObject.setName(topic);
                        TopicConfig topicConfig = allTopicConfig.getTopicConfigTable().get(topic);
                        if(topicConfig != null) {
                            topicObject.setOrdered(topicConfig.isOrder() ? 1 : 0);
                            topicObject.setQueueNum(topicConfig.getReadQueueNums());
                            try {
                                topicService.save(topicObject);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            logger.error("topic:{} no config", topic);
                        }
                    }
                }
                public Cluster mqCluster() {
                    return mqCluter;
                }
            });
        }
    }
}
