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
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserProducer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserProducerDaoTest {

    @Autowired
    private UserProducerDao userProducerDao;
    
    @Autowired
    private UserDao userDao;
    
    @Autowired
    private TopicDao topicDao;
    
    @Test
    public void testInsert() {
        UserProducer userProducer = new UserProducer();
        userProducer.setUid(getUserId());
        Topic topic = getTopic();
        userProducer.setTid(topic.getId());
        userProducer.setProducer("tmp-"+topic.getName()+"-producer");
        userProducerDao.insert(userProducer);
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
    public void testSelectTidByProducerAndUid() {
        long uid = 0L;
        String producer = "test-producer-group";
        List<Long> result = userProducerDao.selectTidByProducerAndUid(uid, producer);
        Assert.assertTrue(result.size() > 0);
    }
}
