package com.sohu.tv.mq.cloud.dao;

import java.util.ArrayList;
import java.util.List;

import com.sohu.tv.mq.cloud.bo.TopicConsumer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.service.ClusterService;

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
        List<String> timeList = new ArrayList<String>();
        timeList.add("1710");
        timeList.add("1711");
        timeList.add("1712");
        timeList.add("1713");
        timeList.add("1714");
        timeList.add("1715");
        timeList.add("1716");
        timeList.add("1717");
        timeList.add("1718");
        timeList.add("1719");
        List<Integer> list = new ArrayList<>();
        list.add(1);
        List<TopicTraffic> result = topicTrafficDao.selectByDateTime("2018-07-31", timeList, list);
        Integer rst = topicDao.updateCount(result);
        Assert.assertEquals(timeList.size(), rst.intValue());
    }

    @Test
    public void testQueryTopicConsumer() {
        List<TopicConsumer> list = topicDao.selectTopicConsumerByTid(1809L);
        Assert.assertNotNull(list);
    }
}
