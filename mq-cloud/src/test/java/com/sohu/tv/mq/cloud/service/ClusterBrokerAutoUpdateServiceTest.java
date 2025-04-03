package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.util.Result;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ClusterBrokerAutoUpdateServiceTest {
    @Autowired
    private ClusterBrokerAutoUpdateService clusterBrokerAutoUpdateService;

    @Autowired
    private ClusterService clusterService;

    @Test
    public void testSave() {
        int cid = clusterService.queryAll().getResult().get(0).getId();
        Result<?> result = clusterBrokerAutoUpdateService.save(cid, 0);
        Assert.assertTrue(result.isOK());
    }

    @Test
    public void testAutoUpdate() throws InterruptedException {
        clusterBrokerAutoUpdateService.autoUpdate();
    }
}