package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class BrokerTrafficServiceTest {

    @Autowired
    private BrokerTrafficService brokerTrafficService;

    @Test
    public void testUpdateBrokerDayTraffic(){
        brokerTrafficService.updateBrokerDayTraffic();
    }

}