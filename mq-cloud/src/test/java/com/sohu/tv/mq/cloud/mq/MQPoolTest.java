package com.sohu.tv.mq.cloud.mq;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.service.ClusterService;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.rocketmq.client.MQAdmin;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MQPoolTest {

    @Autowired
    private GenericKeyedObjectPool<Cluster, MQAdminExt> mqPool;

    @Autowired
    private ClusterService clusterService;

    @Test
    public void test() throws Exception {
        Cluster cluster = clusterService.getMQClusterById(1);
        MQAdminExt mqAdmin1 = mqPool.borrowObject(cluster);
        Assert.assertNotNull(mqAdmin1);
        System.out.println(((DefaultMQAdminExt) mqAdmin1).getAdminExtGroup());
        MQAdminExt mqAdmin2 = mqPool.borrowObject(cluster);
        Assert.assertNotNull(mqAdmin2);
        System.out.println(((DefaultMQAdminExt) mqAdmin2).getAdminExtGroup());
        mqPool.returnObject(cluster, mqAdmin1);
        MQAdminExt mqAdmin3 = mqPool.borrowObject(cluster);
        Assert.assertNotNull(mqAdmin3);
        System.out.println(((DefaultMQAdminExt) mqAdmin3).getAdminExtGroup());
    }
}
