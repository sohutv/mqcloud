package com.sohu.tv.mq.cloud.web.controller.operate;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.admin.operate.AutoOperateProxyController;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AutoOperateProxyControllerTest extends AutoOperateTest {

    @Autowired
    private AutoOperateProxyController autoOperateProxyController;

    @Test
    public void testRegister() {
        Result<?> result = autoOperateProxyController.register(addr(), token(), request());
        Assert.assertEquals(true, result.isOK());
    }

    @Test
    public void testUnregister() {
        Result<?> result = autoOperateProxyController.unregister(addr(), token(), request());
        Assert.assertEquals(true, result.isOK());
    }
}