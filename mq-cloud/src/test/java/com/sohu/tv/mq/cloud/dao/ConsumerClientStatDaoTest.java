package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ConsumerClientStat;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * @author yongweizhao
 * @create 2019/11/7 9:59
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConsumerClientStatDaoTest {

    @Autowired
    private ConsumerClientStatDao consumerClientStatDao;

    @Test
    public void testInsert() {
        Integer result = null;
        for (int i = 1; i < 6; i ++) {
            ConsumerClientStat ccs = new ConsumerClientStat();
            ccs.setConsumer("test" + i + "-consumer");
            ccs.setClient(i + ".2.3.4");
            result = consumerClientStatDao.saveConsumerClientStat(ccs);
        }
        Assert.assertTrue(result > 0);
    }

    @Test
    public void testSelect() {
        Date startTime = new Date(1573453823000L);
        String client = "5.5.5.5";
        List<String> result = consumerClientStatDao.selectByDateAndClient(client, startTime);
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void testDelete() {
        Date time = new Date(1572597113000L);
        Integer result = consumerClientStatDao.delete(time);
        Assert.assertTrue(result > 0);
    }
}
