package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.util.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AlarmConfigBridingServiceTest {

    @Autowired
    private AlarmConfigBridingService alarmConfigBridingService;
    
    @Autowired
    private AlarmConfigService alarmConfigService;

    @Test
    public void test() throws Exception {
        Result<List<AlarmConfig>> userAlarmConfigResult = alarmConfigService.queryAll();
        if (userAlarmConfigResult.isNotOK()) {
            System.out.println("refresh user alarm config err");
            return;
        }
        alarmConfigBridingService.setConfigTable(userAlarmConfigResult.getResult());
        
        String[] consumer = {"pgc-sub-letter-group", "uc_order_consumer_online", "rocketmq-dm-ctr",
                "vrsCountQueue9ConsumerGroup"};
        for (int i = 0; i < consumer.length; i++) {
            long count = alarmConfigBridingService.getConsumerFailCount(consumer[i]);
            System.out.println(count);
            long accumulateCount = alarmConfigBridingService.getAccumulateCount(consumer[i]);
            System.out.println(accumulateCount);
            long accumulateTime = alarmConfigBridingService.getAccumulateTime(consumer[i]);
            System.out.println(accumulateTime);
            long blockTime = alarmConfigBridingService.getBlockTime(consumer[i]);
            System.out.println(blockTime);
        }

    }
    
    @Test
    public void testNeedWarn() throws Exception {
        boolean warn = alarmConfigBridingService.needWarn("abc","def","pgc-sub-letter-group");
        System.out.println(warn);
        //Thread.sleep(10 * 1000);
        warn = alarmConfigBridingService.needWarn("abc","def","pgc-sub-letter-group");
        System.out.println(warn);
        warn = alarmConfigBridingService.needWarn("abc","def","pgc-sub-letter-group");
        System.out.println(warn);
        Thread.sleep(10 * 1000);
        warn = alarmConfigBridingService.needWarn("abc","def","ghi");
        System.out.println(warn);
    }
}
