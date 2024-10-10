package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicConsumer;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicDaoTest {

    @Autowired
    private TopicDao topicDao;
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private TopicTrafficDao topicTrafficDao;
    
    @Test
    public void testInsert() {
        Topic topic = new Topic();
        topic.setClusterId(clusterService.getMQClusterById(2).getId());
        topic.setName("test");
        topic.setOrdered(Topic.NO_ORDER);
        topic.setQueueNum(8);
        topicDao.insert(topic);
        Assert.assertTrue(topic.getId() > 0);
    }
    
    @Test
    public void testInsertLoop() {
        for(int i = 0; i < 10; ++i) {
            Topic topic = new Topic();
            topic.setClusterId(clusterService.getMQClusterById(2).getId());
            topic.setName("test"+i);
            topic.setOrdered(Topic.NO_ORDER);
            topic.setQueueNum(8);
            topicDao.insert(topic);
            Assert.assertTrue(topic.getId() > 0);
        }
    }

    @Test
    public void testUpdateCount() {
        Map<String, List<String>> timeMap = DateUtil.getBefore5Minute();
        for (Map.Entry<String, List<String>> entry : timeMap.entrySet()) {
            List<TopicTraffic> topicTrafficList = topicTrafficDao.selectByDateTime(entry.getKey(), entry.getValue());
            for(TopicTraffic topicTraffic : topicTrafficList) {
                Integer rst = topicDao.updateCount(topicTraffic);
                Assert.assertEquals(topicTrafficList.size(), rst.intValue());
            }
        }
    }
    
    @Test
    public void testQueryTopicConsumer() {
        List<TopicConsumer> list = topicDao.selectTopicConsumerByTid(1809L);
        Assert.assertNotNull(list);
    }
    
    @Test
    public void testRestCount() {
        Date dt = new Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000);
        dt = DateUtil.parseYMD(DateUtil.formatYMD(dt));
        Integer count = topicDao.resetCount(dt);
        Assert.assertNotNull(count);
    }

}
