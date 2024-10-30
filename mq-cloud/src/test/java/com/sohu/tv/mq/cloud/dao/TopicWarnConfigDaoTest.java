package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.TopicWarnConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicWarnConfigDaoTest {

    @Autowired
    private TopicWarnConfigDao topicWarnConfigDao;

    @Test
    public void testInsert() {
        TopicWarnConfig topicWarnConfig = new TopicWarnConfig();
        topicWarnConfig.setId(1L);
        topicWarnConfig.setOperandType(1);
        topicWarnConfig.setOperatorType(1);
        topicWarnConfig.setThreshold(1);
        topicWarnConfig.setWarnInterval(2);
        Integer result = topicWarnConfigDao.insert(topicWarnConfig);
        Assert.assertNotNull(result);
    }

    @Test
    public void testSelect() {
        List<TopicWarnConfig> topicWarnConfigList = topicWarnConfigDao.selectByTid(0);
        Assert.assertNotNull(topicWarnConfigList);
    }

    @Test
    public void testDelete() {
        Integer count = topicWarnConfigDao.delete(1);
        Assert.assertTrue(count > 0);
    }

}