package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.util.Result;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserConsumerServiceTest {
    
    @Autowired
    private UserConsumerService userConsumerService;

    @Test
    public void testQueryUserByConsumer() {
        long tid = 360;
        long cid = 641;
        Result<List<User>> userListResult = userConsumerService.queryUserByConsumer(tid, cid);
        String email = null;
        if(userListResult.isNotEmpty()) {
            StringBuilder sb = new StringBuilder();
            for(User u : userListResult.getResult()) {
                sb.append(u.getEmail());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            email = sb.toString();
        }
        System.out.println(email);
        Assert.assertNotNull(email);
    }

    @Test
    public void test(){
        Cluster cluster = new Cluster();
        cluster.setId(3);
        cluster.setName("test-cluster");
        Consumer consumer = new Consumer();
        consumer.setName("mqcloud-json-test-consumer");
        Result result = userConsumerService.createAndUpdateConsumerOnCluster(cluster, consumer);
        Assert.assertTrue(result.isOK());
    }

}
