package com.sohu.tv.mq.cloud.dao;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
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
        List<TopicTraffic> result = topicTrafficDao.select(1, "20180626");
        Assert.assertNotNull(result);
    }
    
    @Test
    public void testSelectByDateTime() {
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
        Assert.assertNotNull(result);
    }

    @Test
    public void testSelectByCreateDateAndTime() {
        List<String> timeList = new ArrayList<String>();
        timeList.add("1653");
        timeList.add("1656");
        timeList.add("1652");
        timeList.add("1651");
        timeList.add("1624");
        timeList.add("1620");
        timeList.add("1620");
        timeList.add("1621");
        timeList.add("1621");
        timeList.add("1622");
        timeList.add("1622");
        List<TopicTraffic> res = topicTrafficDao.selectByCreateDateAndTime(700, "2020-08-21", timeList);
        System.out.println(res.size());
        for(TopicTraffic topicTraffic : res) {
            System.out.println(topicTraffic.getCreateTime());
        }
    }
}
