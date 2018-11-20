package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.util.Result;

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

}
