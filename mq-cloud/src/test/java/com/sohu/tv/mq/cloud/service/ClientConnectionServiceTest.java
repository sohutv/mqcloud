package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.Result;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.protocol.body.Connection;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.common.protocol.body.ProducerConnection;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ClientConnectionServiceTest{

    @Autowired
    private ClientConnectionService clientConnectionService;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Test
    public void testScanAndSave(){
        clientConnectionService.scanAllClientGroupConnectLanguage(null);
    }

    @Test
    public void testFetchPythonProducerConnection(){
        Result<ProducerConnection> producerConnectionResult = mqAdminTemplate.execute(new MQAdminCallback<Result<ProducerConnection>>(){
            @Override
            public Result<ProducerConnection> callback(MQAdminExt mqAdmin) throws Exception {
                ProducerConnection producerConnection = mqAdmin.examineProducerConnectionInfo("sccd-sensitive_det-product-topic-producer",
                        "sccd-sensitive_det-product-topic");
                return Result.getResult(producerConnection);
            }

            @Override
            public Result<ProducerConnection> exception(Exception e) throws Exception {
                e.printStackTrace();
                return Result.getDBErrorResult(e);
            }

            @Override
            public Cluster mqCluster() {
                Cluster cluster = new Cluster();
                cluster.setId(1);
                return cluster;
            }
        });
    }

    // test
    @Test
    public void testFetchGoConsumerRunningInfo(){
        mqAdminTemplate.execute(new MQAdminCallback<Map<String, ConsumerRunningInfo>>() {
            public Map<String, ConsumerRunningInfo> callback(MQAdminExt mqAdmin) throws Exception {
                Map<String, ConsumerRunningInfo> infoMap = new HashMap<String, ConsumerRunningInfo>();
                // 获取consumer链接
                ConsumerConnection consumerConnection = mqAdmin.examineConsumerConnectionInfo("go-test-sub-consumer");
                // 获取运行时信息
                for (Connection connection : consumerConnection.getConnectionSet()) {
                    if (connection.getVersion() < MQVersion.Version.V3_1_8_SNAPSHOT.ordinal()) {
                        continue;
                    }
                    String clientId = connection.getClientId();
                    ConsumerRunningInfo consumerRunningInfo = mqAdmin.getConsumerRunningInfo("go-test-sub-consumer", clientId,
                            false);
                    if (consumerRunningInfo != null) {
                        infoMap.put(clientId, consumerRunningInfo);
                    }
                }
                return infoMap;
            }

            public Map<String, ConsumerRunningInfo> exception(Exception e) throws Exception {
                return null;
            }

            @Override
            public Cluster mqCluster() {
                Cluster cluster = new Cluster();
                cluster.setId(5);
                return cluster;
            }
        });
    }

    // online
    @Test
    public void testFetchCPPConsumerRunningInfo(){
        mqAdminTemplate.execute(new MQAdminCallback<Map<String, ConsumerRunningInfo>>() {
            public Map<String, ConsumerRunningInfo> callback(MQAdminExt mqAdmin) throws Exception {
                Map<String, ConsumerRunningInfo> infoMap = new HashMap<String, ConsumerRunningInfo>();
                // 获取consumer链接
                ConsumerConnection consumerConnection = mqAdmin.examineConsumerConnectionInfo("basic-ndfs-upload-img-detection-test-topic-consumer");
                // 获取运行时信息
                for (Connection connection : consumerConnection.getConnectionSet()) {
                    if (connection.getVersion() < MQVersion.Version.V3_1_8_SNAPSHOT.ordinal()) {
                        continue;
                    }
                    String clientId = connection.getClientId();
                    ConsumerRunningInfo consumerRunningInfo = mqAdmin.getConsumerRunningInfo("basic-ndfs-upload-img-detection-test-topic-consumer", clientId,
                            false);
                    if (consumerRunningInfo != null) {
                        infoMap.put(clientId, consumerRunningInfo);
                    }
                    break;
                }
                return infoMap;
            }

            public Map<String, ConsumerRunningInfo> exception(Exception e) throws Exception {
                return null;
            }

            @Override
            public Cluster mqCluster() {
                Cluster cluster = new Cluster();
                cluster.setId(3);
                return cluster;
            }
        });
    }

}