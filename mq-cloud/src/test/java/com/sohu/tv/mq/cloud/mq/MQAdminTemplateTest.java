package com.sohu.tv.mq.cloud.mq;

import java.util.List;
import java.util.Properties;

import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.util.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MQAdminTemplateTest {

    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    @Autowired
    private BrokerService brokerService;
    
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

    @Test
    public void testExamineTopicRouteInfo() {
        mqAdminTemplate.execute(new DefaultInvoke() {
            public Cluster mqCluster() {
                return clusterService.getMQClusterById(2);
            }

            @Override
            public void invoke(MQAdminExt mqAdmin) throws Exception {
                TopicRouteData route = mqAdmin.examineTopicRouteInfo("search-core_model_v2-topic");
                Assert.assertNotNull(route);
            }
        });
    }
    
    @Test
    public void testBrokerConfig() {
        int cid = 2;
        Result<List<Broker>> result = brokerService.query(cid);
        if(result.isEmpty()) {
            return;
        }
        mqAdminTemplate.execute(new DefaultInvoke() {
            public Cluster mqCluster() {
                return clusterService.getMQClusterById(cid);
            }

            @Override
            public void invoke(MQAdminExt mqAdmin) throws Exception {
                Properties properties = mqAdmin.getBrokerConfig(result.getResult().get(0).getAddr());
                Assert.assertNotNull(properties);
            }
        });
    }
}
