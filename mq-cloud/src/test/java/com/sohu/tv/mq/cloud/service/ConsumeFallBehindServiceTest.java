package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.common.model.BrokerMomentStatsData;
import com.sohu.tv.mq.cloud.util.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConsumeFallBehindServiceTest {

    @Autowired
    private ConsumeFallBehindService consumeFallBehindService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private BrokerService brokerService;

    @Test
    public void test() {
        Cluster cluster = clusterService.getMQClusterById(5);
        Result<List<Broker>> result = brokerService.query(cluster.getId());
        for (Broker boker : result.getResult()) {
            Result<BrokerMomentStatsData> brokerMomentStatsDataResult = consumeFallBehindService
                    .getConsumeFallBehindSize(boker.getAddr(), cluster, 1);
            BrokerMomentStatsData brokerMomentStatsData = brokerMomentStatsDataResult.getResult();
            Assert.assertTrue(brokerMomentStatsData.getBrokerMomentStatsItemList().size() > 0);
        }
    }

}
