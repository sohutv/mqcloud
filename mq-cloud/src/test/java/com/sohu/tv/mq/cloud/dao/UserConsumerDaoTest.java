package com.sohu.tv.mq.cloud.dao;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserConsumer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserConsumerDaoTest {
    
    @Autowired
    private UserConsumerDao userConsumerDao;

    @Autowired
    private UserDao userDao;
    
    @Autowired
    private TopicDao topicDao;
    
    @Autowired
    private ConsumerDao consumerDao;
    
    @Test
    public void testInsert() {
        UserConsumer userConsumer = new UserConsumer();
        userConsumer.setUid(getUserId());
        Topic topic = getTopic();
        userConsumer.setTid(topic.getId());
        List<Consumer> list = consumerDao.selectByTid(topic.getId());
        userConsumer.setConsumerId(list.get(0).getId());
        userConsumerDao.insert(userConsumer);
    }

    @Test
    public void testSelectByUid() {
        fail("Not yet implemented");
    }
    
    private long getUserId() {
        User user = userDao.selectByEmail("yongfeigao");
        return user.getId();
    }
    
    private Topic getTopic() {
        List<Long> idList = new ArrayList<Long>();
        idList.add(1L);
        Topic topic = topicDao.selectByIdList(idList).get(1);
        return topic;
    }

    @Test
    public void testSelectTidByUidAndConsumer() {
        long uid = 0L;
        String consumer = "testConsumerGroup";
        List<Long> result = userConsumerDao.selectTidByUidAndConsumer(uid, consumer);
        Assert.assertTrue(result.size() > 0);
    }
}
