package com.sohu.tv.mq.cloud.service;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserServiceTest {
    
    @Autowired
    private UserService userService;

    @Test
    public void testInsert() {
        User user = new User();
        user.setName("yongfeigao");
        user.setEmail("yongfeigao@xxx.com");
        user.setMobile("18611111111");
        user.setType(User.ADMIN);
        Result<User> result = userService.save(user);
        Assert.assertEquals(Status.DB_DUPLICATE_KEY.getKey(), result.getStatus());
    }

    @Test
    public void testSelectByName() {
        String name = "yongfeigao";
        Result<User> result = userService.queryByEmail(name);
        Assert.assertNotNull(result.getResult());
    }

    @Test
    public void testUpdateUserType() {
        fail("Not yet implemented");
    }

}
