package com.sohu.tv.mq.cloud.service;

import org.apache.rocketmq.tools.monitor.UndoneMsgs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.task.monitor.SohuMonitorListener;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SohuMonitorListenerTest {
    
    @Autowired
    private SohuMonitorListener sohuMonitorListener;

    @Test
    public void testAccumulateWarn() {
        UndoneMsgs undoneMsgs = new UndoneMsgs();
        undoneMsgs.setTopic("vrs-topic");
        undoneMsgs.setConsumerGroup("vrs-topic-api-consumer");
        undoneMsgs.setUndoneMsgsTotal(100000);
        undoneMsgs.setUndoneMsgsSingleMQ(100);
        undoneMsgs.setUndoneMsgsDelayTimeMills(1000000);
        sohuMonitorListener.accumulateWarn(undoneMsgs);
    }
    
    @Test
    public void testReportUndoneMsgs() {
        UndoneMsgs undoneMsgs = new UndoneMsgs();
        undoneMsgs.setTopic("pgc-sub-topic");
        undoneMsgs.setConsumerGroup("pgc-sub-group-consumer");
        undoneMsgs.setUndoneMsgsTotal(100000);
        undoneMsgs.setUndoneMsgsSingleMQ(100);
        undoneMsgs.setUndoneMsgsDelayTimeMills(1000000);
        sohuMonitorListener.reportUndoneMsgs(undoneMsgs) ;
    }
    
}
