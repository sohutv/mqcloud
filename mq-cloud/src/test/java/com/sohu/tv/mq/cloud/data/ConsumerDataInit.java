package com.sohu.tv.mq.cloud.data;

import java.util.List;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.body.GroupList;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.mq.DefaultInvoke;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.util.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConsumerDataInit {

    @Autowired
    private TopicService topicService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    @Autowired
    private ClusterService clusterService;
    
    @Test
    public void test() {
        Cluster mqCluster = clusterService.getMQClusterById(2);
        Topic topic = topicService.queryTopic("pushLocal").getResult();
        mqAdminTemplate.execute(new DefaultInvoke() {
            public Cluster mqCluster() {
                return mqCluster;
            }
            public void invoke(MQAdminExt mqAdmin) throws Exception {
                GroupList groupList = mqAdmin.queryTopicConsumeByWho(topic.getName());
                for (String group : groupList.getGroupList()) {
                    ConsumerConnection conn = null;
                    try {
                        conn = mqAdmin.examineConsumerConnectionInfo(group);
                    } catch (MQBrokerException e) {
                        if (206 == e.getResponseCode()) {
                            continue;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (conn == null) {
                        continue;
                    }
                    Consumer consumer = new Consumer();
                    if (MessageModel.BROADCASTING == conn.getMessageModel()) {
                        consumer.setConsumeWay(Consumer.BROADCAST);
                    }
                    consumer.setName(group);
                    consumer.setTid(topic.getId());
                    try {
                        consumerService.save(consumer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Test
    public void initConsumerData() {
        for (Cluster mqCluster : clusterService.getAllMQCluster()) {
            Result<List<Topic>> topicListResult = topicService.queryTopicList(mqCluster);
            if (!topicListResult.isOK()) {
                continue;
            }
            List<Topic> topicList = topicListResult.getResult();
            mqAdminTemplate.execute(new DefaultInvoke() {
                public Cluster mqCluster() {
                    return mqCluster;
                }
                public void invoke(MQAdminExt mqAdmin) throws Exception {
                    for (Topic topic : topicList) {
                        GroupList groupList = null;
                        try {
                            groupList = mqAdmin.queryTopicConsumeByWho(topic.getName());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            continue;
                        }
                        for (String group : groupList.getGroupList()) {
                            ConsumerConnection conn = null;
                            try {
                                conn = mqAdmin.examineConsumerConnectionInfo(group);
                            } catch (MQBrokerException e) {
                                if (206 == e.getResponseCode()) {
                                    continue;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (conn == null) {
                                continue;
                            }
                            Consumer consumer = new Consumer();
                            if (MessageModel.BROADCASTING == conn.getMessageModel()) {
                                consumer.setConsumeWay(Consumer.BROADCAST);
                            }
                            consumer.setName(group);
                            consumer.setTid(topic.getId());
                            try {
                                consumerService.save(consumer);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }
}
