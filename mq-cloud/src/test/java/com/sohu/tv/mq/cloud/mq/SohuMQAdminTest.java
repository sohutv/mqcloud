package com.sohu.tv.mq.cloud.mq;

import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.client.QueryResult;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageClientIDSetter;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.serializable.DefaultMessageSerializer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SohuMQAdminTest {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @SuppressWarnings("rawtypes")
    private DefaultMessageSerializer serializer = new DefaultMessageSerializer();

    @Test
    public void testSendMessage() {
        SendResult sendResult = mqAdminTemplate.execute(new DefaultCallback<SendResult>() {
            @SuppressWarnings("unchecked")
            public SendResult callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                Message msg = new Message();
                msg.setTopic("basic-apitest-topic");
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("a", "b");
                map.put("c", "d");
                map.put("o", "mqcloud");
                msg.setBody(serializer.serialize(map));
                return sohuMQAdmin.sendMessage(msg);
            }

            public Cluster mqCluster() {
                return clusterService.getMQClusterById(3);
            }
        });
        System.out.println(sendResult);
        Assert.assertEquals(SendStatus.SEND_OK, sendResult.getSendStatus());
    }

    @Test
    public void testGetConsumeThreadMetrics() {
        String consumerGroup = "basic-apitest-topic-consumer";
        String clientId = "127.0.0.1@5372@3";
        long timeoutMillis = 5000;
        ConsumerRunningInfo consumerRunningInfo  = mqAdminTemplate.execute(new DefaultCallback<ConsumerRunningInfo>() {
            public ConsumerRunningInfo callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                return sohuMQAdmin.getConsumeThreadMetrics(consumerGroup, clientId, timeoutMillis);
            }

            public Cluster mqCluster() {
                return clusterService.getMQClusterById(3);
            }
        });
        System.out.println(consumerRunningInfo);
    }
    
    @Test
    public void testConsumeTimespanMessage() {
        String topic = MixAll.getDLQTopic("basic-apitest-topic-consumer");
        String group = "basic-apitest-topic-consumer";
        String clientId = "127.0.0.1@5476@5";
        long startTimestamp = 1637305200000L;
        long endTimestamp = 1637305500000L;
        mqAdminTemplate.execute(new MQAdminCallback<Void>() {
            public Void callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                sohuMQAdmin.consumeTimespanMessage(clientId, topic, group, startTimestamp, endTimestamp);
                return null;
            }
            public Cluster mqCluster() {
                return clusterService.getMQClusterById(5);
            }
            @Override
            public Void exception(Exception e) throws Exception {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Test
    public void testQueryTimerMessage() {
        mqAdminTemplate.execute(new MQAdminCallback<Void>() {
            public Void callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                String uniqKey = "0A0728383CB018B4AAC2037F3FE50001";
                Long beginTime = MessageClientIDSetter.getNearlyTimeFromID(uniqKey).getTime() - 5 * 60 * 1000L;
                QueryResult queryResult = sohuMQAdmin.queryTimerMessageByUniqKey("broker-2", uniqKey
                        , beginTime, 0L, true);
                Assert.assertTrue(queryResult.getMessageList().size() > 1);
                return null;
            }

            public Cluster mqCluster() {
                return clusterService.getMQClusterById(7);
            }

            @Override
            public Void exception(Exception e) throws Exception {
                e.printStackTrace();
                return null;
            }
        });
    }
}
