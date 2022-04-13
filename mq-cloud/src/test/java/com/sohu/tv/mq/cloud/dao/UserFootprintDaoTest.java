package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.UserFootprint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author: yongfeigao
 * @date: 2022/3/9 15:51
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserFootprintDaoTest {

    @Autowired
    private UserFootprintDao userFootprintDao;

    @Test
    public void testInsert() {
        UserFootprint up = new UserFootprint();
        up.setTid(1);
        up.setUid(2);
        Integer rst = userFootprintDao.insert(up);
        Assert.assertNotNull(rst);
    }

    @Test
    public void testSelectCount() {
        Integer rst = userFootprintDao.selectCount(2);
        Assert.assertEquals(1, rst.intValue());
    }

    @Test
    public void testSelect() {
        List<UserFootprint> rst = userFootprintDao.selectByPage(2, 0, 1);
        Assert.assertEquals(1, rst.size());
    }

    @Test
    public void testDelete() {
        Integer rst = userFootprintDao.deleteByTid(1);
        Assert.assertEquals(1, rst.intValue());
    }
}