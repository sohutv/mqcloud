package com.sohu.tv.mq.cloud.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.util.DateUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicTrafficServiceTest {
    
    @Autowired
    private TopicTrafficService topicTrafficService;
    
    @Autowired
    private ClusterService clusterService;

    @Test
    public void testSave() {
        TopicTraffic topicTraffic = new TopicTraffic();
        topicTraffic.setTid(1);
        topicTraffic.setSize(100);
        topicTraffic.setCount(3232);
        topicTraffic.setCreateTime(DateUtil.getFormatNow(DateUtil.HHMM));
        topicTrafficService.save(topicTraffic);
    }

    @Test
    public void testCollectTraffic() {
        topicTrafficService.collectTraffic(clusterService.getMQClusterById(1));
    }
    
}
