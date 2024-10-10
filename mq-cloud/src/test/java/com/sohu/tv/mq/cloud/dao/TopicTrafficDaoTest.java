package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicTrafficDaoTest {
    
    @Autowired
    private TopicTrafficDao topicTrafficDao;

    @Test
    public void testInsert() {
        TopicTraffic topicTraffic = new TopicTraffic();
        topicTraffic.setTid(1);
        topicTraffic.setCount(10086);
        topicTraffic.setSize(10010);
        topicTraffic.setCreateTime(DateUtil.getFormatNow(DateUtil.HHMM));
        topicTrafficDao.insert(topicTraffic);
    }
    
    @Test
    public void testQuery() {
        List<TopicTraffic> result = topicTrafficDao.select(1, DateUtil.parse(DateUtil.YMD, "20180626"));
        Assert.assertNotNull(result);
    }

    @Test
    public void testSelectByDateTime() {
        Map<String, List<String>> timeMap = DateUtil.getBefore5Minute();
        for (Map.Entry<String, List<String>> entry : timeMap.entrySet()) {
            List<TopicTraffic> topicTrafficList = topicTrafficDao.selectByDateTime(entry.getKey(), entry.getValue());
            Assert.assertNotNull(topicTrafficList);
        }
    }

    @Test
    public void testSelectByCreateDateAndTime() {
        Map<String, List<String>> timeMap = DateUtil.getBefore5Minute();
        for (Map.Entry<String, List<String>> entry : timeMap.entrySet()) {
            List<TopicTraffic> res = topicTrafficDao.selectByCreateDateAndTime(494, entry.getKey(), entry.getValue());
            Assert.assertNotNull(res);
        }
    }
}
