package com.sohu.tv.mq.cloud.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.User;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserDaoTest {

    @Autowired
    private UserDao userDao;
    
    @Test
    public void testInsert() {
        User user = new User();
        user.setName("yongfeigao");
        user.setEmail("yongfeigao@xxx.com");
        user.setMobile("18611111111");
        user.setType(User.ADMIN);
        userDao.insert(user);
        Assert.assertTrue(user.getId() > 0);
    }
    
    @Test
    public void testSelectByName() {
        User user = userDao.selectByEmail("yongfeigao");
        Assert.assertNotNull(user);
    }
    
    @Test
    public void testDelete() {
        User user = userDao.selectByEmail("yongfeigao");
        Integer count = userDao.delete(user.getId());
        Assert.assertEquals(1, count.intValue());
    }

    @Test
    public void selectConsumerUserListByTidTest() {
        long tid = 401L;
        List<User> userList = userDao.selectConsumerUserListByTid(tid);
        Assert.assertTrue(userList.size() > 0);
    }

    @Test
    public void selectProducerUserListByTidTest() {
        long tid = 401L;
        List<User> userList = userDao.selectProducerUserListByTid(tid);
        Assert.assertTrue(userList.size() > 0);
    }
}
