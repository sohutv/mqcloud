package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.vo.ClusterCapacityVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ClusterCapacityServiceTest {

    @Autowired
    private ClusterCapacityService clusterCapacityService;

    @Test
    public void testGetClusterCapacity() {
        Result<ClusterCapacityVO> result = clusterCapacityService.getClusterCapacity();
        Assert.assertNotNull(result);
    }

    @Test
    public void testSendCapacityDailyMail() {
        Result<?> result = clusterCapacityService.sendCapacityDailyMail();
        Assert.assertNotNull(result);
    }
}