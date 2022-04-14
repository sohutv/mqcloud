package com.sohu.tv.mq.cloud.dao;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.UserFavorite;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 用户收藏测试
 * @author: yongfeigao
 * @date: 2022/3/21 15:42
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserFavoriteDaoTest {

    @Autowired
    private UserFavoriteDao userFavoriteDao;

    @Test
    public void testInsert() {
        UserFavorite uf = new UserFavorite();
        uf.setTid(1);
        uf.setUid(2);
        Integer rst = userFavoriteDao.insert(uf);
        Assert.assertNotNull(rst);
    }

    @Test
    public void testSelectCount() {
        Integer rst = userFavoriteDao.selectCount(2);
        Assert.assertEquals(1, rst.intValue());
    }

    @Test
    public void testSelect() {
        List<UserFavorite> rst = userFavoriteDao.selectByPage(2, 0, 1);
        Assert.assertEquals(1, rst.size());
    }

    @Test
    public void testDelete() {
        Integer rst = userFavoriteDao.deleteByTid(1);
        Assert.assertEquals(1, rst.intValue());
    }
}