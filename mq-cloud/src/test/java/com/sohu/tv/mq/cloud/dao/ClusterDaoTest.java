package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Cluster;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ClusterDaoTest {
    
    @Autowired
    private ClusterDao clusterDao;

    @Test
    public void test() {
        List<Cluster> list = clusterDao.select();
        Assert.assertNotNull(list);
    }

}
