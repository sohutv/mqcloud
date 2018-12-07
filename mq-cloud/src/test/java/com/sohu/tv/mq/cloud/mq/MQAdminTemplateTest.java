package com.sohu.tv.mq.cloud.mq;

import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.service.ClusterService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MQAdminTemplateTest {

    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    @Test
    public void test() {
        ClusterInfo clusterInfo = mqAdminTemplate.execute(new DefaultCallback<ClusterInfo>() {
            public ClusterInfo callback(MQAdminExt mqAdmin) throws Exception {
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                return clusterInfo;
            }
            public Cluster mqCluster() {
                return clusterService.getMQClusterById(2);
            }
        });
        Assert.assertNotNull(clusterInfo);
    }
    
    @Test
    public void testException() {
        ClusterInfo clusterInfo = mqAdminTemplate.execute(new MQAdminCallback<ClusterInfo>() {
            public ClusterInfo callback(MQAdminExt mqAdmin) throws Exception {
                throw new RuntimeException("only for test");
            }
            public Cluster mqCluster() {
                return clusterService.getMQClusterById(2);
            }
            public ClusterInfo exception(Exception e) throws Exception {
                e.printStackTrace();
                return new ClusterInfo();
            }
        });
        Assert.assertNotNull(clusterInfo);
        System.out.println(clusterInfo);
    }
    
    @Test
    public void testExamineTopicStats() {
        TopicStatsTable topicStatsTable = mqAdminTemplate.execute(new DefaultCallback<TopicStatsTable>() {
            public TopicStatsTable callback(MQAdminExt mqAdmin) throws Exception {
                TopicStatsTable topicStatsTable = mqAdmin.examineTopicStats("%DLQ%ugc-consumer-56-app-data-sync-group");
                return topicStatsTable;
            }
            public Cluster mqCluster() {
                return clusterService.getMQClusterById(1);
            }
        });
        Assert.assertNotNull(topicStatsTable);
    }

}
