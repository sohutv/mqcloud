package com.sohu.tv.mq.cloud.dao;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.util.DateUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class BrokerTrafficDaoTest {
   
    @Autowired
    private BrokerTrafficDao brokerTrafficDao;
    
    private String ip = "127.0.0.1";
    
    private Date date = new Date();

    @Test
    public void testInsert() {
        BrokerTraffic brokerTraffic = new BrokerTraffic();
        brokerTraffic.setIp(ip);
        brokerTraffic.setCreateTime("1618");
        brokerTraffic.setPutCount(122);
        brokerTraffic.setPutSize(12333);
        brokerTraffic.setGetCount(122);
        brokerTraffic.setGetSize(12333);
        Integer result = brokerTrafficDao.insert(brokerTraffic);
        Assert.assertNotNull(result);
    }

    @Test
    public void testDelete() {
        Integer result = brokerTrafficDao.delete(date);
        Assert.assertNotNull(result);
    }

    @Test
    public void testSelect() {
        List<BrokerTraffic> list = brokerTrafficDao.select(ip, DateUtil.formatYMD(date));
        Assert.assertNotNull(list);
    }

}
