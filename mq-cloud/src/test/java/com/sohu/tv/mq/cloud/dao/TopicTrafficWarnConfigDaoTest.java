package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.TopicTrafficWarnConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author yongweizhao
 * @create 2020/10/9 15:30
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicTrafficWarnConfigDaoTest {
    @Autowired
    private TopicTrafficWarnConfigDao topicTrafficWarnConfigDao;

    @Test
    public void testInsert() {
        TopicTrafficWarnConfig topicTrafficWarnConfig = new TopicTrafficWarnConfig();
        topicTrafficWarnConfig.setAvgMultiplier(5.0f);
        topicTrafficWarnConfig.setAvgMaxPercentageIncrease(200f);
        topicTrafficWarnConfig.setMaxMaxPercentageIncrease(30f);
        topicTrafficWarnConfig.setAlarmReceiver(0);
        topicTrafficWarnConfig.setTopic("test-topic");
        Integer result = topicTrafficWarnConfigDao.insertAndUpdate(topicTrafficWarnConfig);
        Assert.assertNotNull(result);
    }

    @Test
    public void testSelect() {
        TopicTrafficWarnConfig topicTrafficWarnConfig = topicTrafficWarnConfigDao.selectByTopicName("test-topic");
        Assert.assertNotNull(topicTrafficWarnConfig);
        Assert.assertNotNull(topicTrafficWarnConfig.getTopic());
    }

    @Test
    public void testDelete() {
        Integer count = topicTrafficWarnConfigDao.delete("test-topic");
        Assert.assertTrue(count > 0);
    }
}
