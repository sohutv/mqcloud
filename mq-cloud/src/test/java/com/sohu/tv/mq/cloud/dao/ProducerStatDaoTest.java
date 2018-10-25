package com.sohu.tv.mq.cloud.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ProducerStat;
import com.sohu.tv.mq.cloud.util.DateUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ProducerStatDaoTest {
    
    @Autowired
    private ProducerStatDao producerStatDao;

    @Test
    public void test() {
        ProducerStat producerStat = new ProducerStat();
        producerStat.setTotalId(3);
        producerStat.setAvg(1.2);
        producerStat.setBroker("213.0123.12:32");
        producerStat.setCount(1);
        producerStat.setMax(2);
        List<ProducerStat> list = new ArrayList<>();
        list.add(producerStat);
        Integer result = producerStatDao.insert(list);
        Assert.assertNotNull(result);
        
        List<ProducerStat> rst = producerStatDao.selectByDate("aaa-producer", NumberUtils.toInt(DateUtil.formatYMDNow()));
        Assert.assertNotNull(rst);
        
        result = producerStatDao.delete(NumberUtils.toInt(DateUtil.formatYMDNow()));
        Assert.assertNotNull(result);
    }

}
