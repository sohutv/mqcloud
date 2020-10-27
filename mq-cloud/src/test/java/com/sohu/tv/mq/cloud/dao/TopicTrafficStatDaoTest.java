package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.TopicTrafficStat;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yongweizhao
 * @create 2020/8/13 11:06
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicTrafficStatDaoTest {

    @Autowired
    private TopicTrafficStatDao topicTrafficStatDao;

    @Test
    public void testInsert() {
        TopicTrafficStat topicTrafficStat = new TopicTrafficStat(12, 100, 200 ,3);
        topicTrafficStatDao.insertAndUpdate(topicTrafficStat);
        topicTrafficStat = new TopicTrafficStat(12, 200, 300 ,4);
        topicTrafficStatDao.insertAndUpdate(topicTrafficStat);
    }

    @Test
    public void testSelect() {
        TopicTrafficStat topicTrafficStat = topicTrafficStatDao.select(12);
        Assert.assertNotNull(topicTrafficStat);
    }

    @Test
    public void testDelete() {
        List<Long> idList = new ArrayList<>();
        idList.add(100L);
        idList.add(101L);
        Integer count = topicTrafficStatDao.delete(idList);
        Assert.assertTrue(count == 2);
    }
}
