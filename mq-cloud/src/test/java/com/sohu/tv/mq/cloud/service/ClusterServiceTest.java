package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.mq.DefaultInvoke;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ClusterServiceTest {

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private ClusterService clusterService;

    @Test
    public void testUpdateFileReservedTime() {
        int cid = 5;
        Result<List<Broker>> brokerListResult = brokerService.query(cid);
        if (brokerListResult.isEmpty()) {
            return;
        }
        Cluster cluster = new Cluster();
        cluster.setId(cid);
        mqAdminTemplate.execute(new DefaultInvoke() {
            public Cluster mqCluster() {
                return cluster;
            }

            @Override
            public void invoke(MQAdminExt mqAdmin) throws Exception {
                List<Broker> brokerList = brokerListResult.getResult();
                for (Broker broker : brokerList) {
                    clusterService.updateFileReservedTime(mqAdmin, cid, broker.getAddr());
                }
            }
        });
    }

}
