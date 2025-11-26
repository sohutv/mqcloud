package com.sohu.tv.mq.cloud.web.controller.operate;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.admin.operate.AutoOperateBrokerController;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AutoOperateBrokerControllerTest extends AutoOperateTest {

    @Autowired
    private AutoOperateBrokerController autoOperateBrokerController;

    @Test
    public void testStartup() {
        Result<?> result = autoOperateBrokerController.startup(addr(), token(), request());
        Assert.assertEquals(true, result.isOK());
    }

    @Test
    public void testShutdown() {
        Result<?> result = autoOperateBrokerController.shutdown(addr(), token(), request());
        Assert.assertEquals(true, result.isOK());
    }
}