package com.sohu.tv.mq.cloud.mq;

import java.util.HashMap;
import java.util.Map;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
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

}
