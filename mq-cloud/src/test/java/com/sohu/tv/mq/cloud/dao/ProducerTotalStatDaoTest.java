package com.sohu.tv.mq.cloud.dao;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ProducerTotalStat;
import com.sohu.tv.mq.cloud.util.DateUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ProducerTotalStatDaoTest {
    
    @Autowired
    private ProducerTotalStatDao producerTotalStatDao;

    @Test
    public void testInsert() {
        ProducerTotalStat producerTotalStat = new ProducerTotalStat();
        producerTotalStat.setAvg(228.3);
        producerTotalStat.setClient("test.mqcloud.com");
        producerTotalStat.setCount(1232);
        Date now = new Date();
        producerTotalStat.setCreateDate(NumberUtils.toInt(DateUtil.formatYMD(now)));
        producerTotalStat.setCreateTime(DateUtil.getFormat(DateUtil.HHMM).format(now));
        producerTotalStat.setPercent90(0);
        producerTotalStat.setPercent99(-1);
        producerTotalStat.setProducer("aaa-producer");
        producerTotalStat.setStatTime((int)(now.getTime()/60000));
        producerTotalStatDao.insert(producerTotalStat);
        Assert.assertTrue(producerTotalStat.getId() > 0);
    }
    
    @Test
    public void testSelect() {
        List<ProducerTotalStat> list = producerTotalStatDao.selectByDate("aaa-producer", 
                NumberUtils.toInt(DateUtil.formatYMDNow()));
        Assert.assertNotNull(list);
    }

    @Test
    public void testDelete() {
        Integer count = producerTotalStatDao.delete(NumberUtils.toInt(DateUtil.formatYMDNow()));
        Assert.assertNotNull(count);
    }
}
