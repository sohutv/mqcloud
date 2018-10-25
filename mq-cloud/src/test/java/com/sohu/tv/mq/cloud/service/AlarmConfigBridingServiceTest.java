package com.sohu.tv.mq.cloud.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AlarmConfigBridingServiceTest {

    @Autowired
    private AlarmConfigBridingService alarmConfigBridingService;

    @Test
    public void testGetConsumerFailCount() throws Exception {
        String topic = "pgc-sub-topic";
        long uid = 40;
        for (int i = 0; i < 5; i++) {
            long count = alarmConfigBridingService.getConsumerFailCount(uid, topic);
            System.out.println(count);
        }

    }
    
    @Test
    public void testNeedWarn() throws Exception {
        String key = "abc";
        boolean warn = alarmConfigBridingService.needWarn(key,"def","ghi");
        System.out.println(warn);
        //Thread.sleep(10 * 1000);
        warn = alarmConfigBridingService.needWarn(key);
        System.out.println(warn);
        warn = alarmConfigBridingService.needWarn(key);
        System.out.println(warn);
        Thread.sleep(10 * 1000);
        warn = alarmConfigBridingService.needWarn(key);
        System.out.println(warn);
    }
}
