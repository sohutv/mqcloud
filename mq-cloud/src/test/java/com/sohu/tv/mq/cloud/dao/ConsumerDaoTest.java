package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.service.ClusterService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ConsumerDaoTest {

    @Autowired
    private ConsumerDao consumerDao;
    
    @Autowired
    private ClusterService clusterService;
    
    @Test
    public void testInsert() {
        Consumer consumer = new Consumer();
        consumer.setConsumeWay(Consumer.BROADCAST);
        consumer.setName("api-vrs-topic-test-consumer");
        consumer.setTid(1);
        consumerDao.insert(consumer);
        Assert.assertTrue(consumer.getId() > 0);
    }

    @Test
    public void testSelectByTid() {
        long tid = 171;
        List<Consumer> list = consumerDao.selectByTid(tid);
        Assert.assertNotNull(list);
    }
    
    @Test
    public void testSelectByClusterId() {
        List<Consumer> list = consumerDao.selectByClusterId(clusterService.getMQClusterById(1).getId());
        Assert.assertNotNull(list);
    }

}
