package com.sohu.tv.mq.cloud.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.util.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MQDeployerTest {
    
    @Autowired
    private MQDeployer mqDeployer;

    @Test
    public void testInitConfig() {
        Result<?> rst = mqDeployer.initConfig("127.0.0.1", "ns");
        Assert.assertEquals(true, rst.isOK());
    }

}
