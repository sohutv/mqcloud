package com.sohu.tv.mq.cloud.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AlertServiceTest {

    @Autowired
    private AlertService alertService;
    
    @Test
    public void testMail() {
        boolean result = alertService.sendMail("123", "456");
        Assert.assertTrue(result);
    }

}
