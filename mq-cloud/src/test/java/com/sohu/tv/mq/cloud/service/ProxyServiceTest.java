package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.common.model.ClientConnectionSize;
import com.sohu.tv.mq.cloud.util.Result;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ProxyServiceTest {

    @Autowired
    private ProxyService proxyService;

    @Test
    public void testGetClientConnectionSize() {
        int cid = 8;
        String addr = proxyService.query(cid).getResult().get(0).getAddr();
        Result<ClientConnectionSize> result = proxyService.getClientConnectionSize(cid, addr);
        Assert.assertNotNull(result.getResult());
    }
}