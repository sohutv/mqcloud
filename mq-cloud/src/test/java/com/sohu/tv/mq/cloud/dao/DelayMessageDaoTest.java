package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class DelayMessageDaoTest {
    
    @Autowired
    private DelayMessageDao delayMessageDao;
    
    @Test
    public void testQuery() {
        List<TopicTraffic> result = delayMessageDao.selectTopicTraffic(299, 20190423);
        Assert.assertNotNull(result);
    }

    @Test
    public void testQueryByIdList() {
       TopicTraffic result = delayMessageDao.selectByIdListDateTime(1303L, 20190424, "0000");
        Assert.assertNotNull(result);
    }
}
