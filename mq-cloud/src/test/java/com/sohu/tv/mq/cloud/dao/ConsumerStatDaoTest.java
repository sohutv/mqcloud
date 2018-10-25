package com.sohu.tv.mq.cloud.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ConsumerStat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConsumerStatDaoTest {
    
    @Autowired
    private ConsumerStatDao consumerStatDao;

    @Test
    public void testSaveSimpleConsumerStat() {
        ConsumerStat consumerStat = new ConsumerStat();
        consumerStat.setConsumerGroup("abc");
        consumerStat.setSbscription("ss");
//        consumerStat.setTopic("");
        int count = consumerStatDao.saveSimpleConsumerStat(consumerStat);
        System.out.println(count+","+consumerStat.getId());
    }

}
