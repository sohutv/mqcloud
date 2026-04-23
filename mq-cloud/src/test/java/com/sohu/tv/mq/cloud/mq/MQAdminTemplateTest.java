package com.sohu.tv.mq.cloud.mq;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.util.Result;
import org.apache.rocketmq.common.CheckRocksdbCqWriteResult;
import org.apache.rocketmq.common.CheckRocksdbCqWriteResult.CheckStatus;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

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

    @Test
    public void deleteRetryTopics() {
        int cid = 8;
        Cluster mqCluster = clusterService.getMQClusterById(cid);
        mqAdminTemplate.execute(new DefaultInvoke() {
            public Cluster mqCluster() {
                return mqCluster;
            }

            public void invoke(MQAdminExt mqAdmin) throws Exception {
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                Iterator<BrokerData> iterator = clusterInfo.getBrokerAddrTable().values().iterator();
                if (!iterator.hasNext()) {
                    return;
                }
                TopicConfigSerializeWrapper ts = mqAdmin.getAllTopicConfig(iterator.next().selectBrokerAddr(), 10000);
                ts.getTopicConfigTable().keySet().stream()
                        .filter(t -> t.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX))
                        .forEach(t -> {
                            try {
                                mqAdmin.deleteTopic(t, mqCluster.getName());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        });
    }

    @Test
    public void testCheckRocksdbCqWriteProgress() {
        int cid = 3;
        Cluster mqCluster = clusterService.getMQClusterById(cid);
        mqAdminTemplate.execute(new DefaultInvoke() {
            public Cluster mqCluster() {
                return mqCluster;
            }

            public void invoke(MQAdminExt mqAdmin) throws Exception {
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                Iterator<BrokerData> iterator = clusterInfo.getBrokerAddrTable().values().iterator();
                while (iterator.hasNext()) {
                    BrokerData brokerData = iterator.next();
                    for (String brokerAddr : brokerData.getBrokerAddrs().values()) {
                        System.out.println("checkRocksdbCqWriteProgress, brokerAddr: " + brokerAddr);
                        long oneDayAgo = System.currentTimeMillis() - 24 * 3600 * 1000L;
                        CheckRocksdbCqWriteResult rst = mqAdmin.checkRocksdbCqWriteProgress(brokerAddr, null, oneDayAgo);
                        Assert.assertEquals(CheckStatus.CHECK_IN_PROGRESS.getValue(), rst.getCheckStatus());
                    }
                }
            }
        });
    }
}
