package com.sohu.tv.mq.cloud.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.sohu.tv.mq.cloud.bo.User;

public class JointerTest {

    @Test
    public void testJoin() {
        List<User> userList = new ArrayList<User>();
        User user = new User();
        user.setEmail("a@sohu.com");
        userList.add(user);
        
        user = new User();
        user.setEmail("b@sohu.com");
        userList.add(user);
        
        String result = Jointer.BY_COMMA.join(userList, u -> u.getEmail());
        Assert.assertEquals("a@sohu.com,b@sohu.com", result);
    }

    @Test
    public void testJoinBlank() {
        List<User> userList = new ArrayList<User>();
        String result = Jointer.BY_COMMA.join(userList, u -> u.getEmail());
        Assert.assertEquals("", result);
        
        userList = null;
        result = Jointer.BY_COMMA.join(userList, u -> u.getEmail());
        Assert.assertEquals("", result);
    }
    
    @Test
    public void testJoinOne() {
        List<User> userList = new ArrayList<User>();
        User user = new User();
        user.setEmail("a@sohu.com");
        userList.add(user);
        
        String result = Jointer.BY_COMMA.join(userList, u -> u.getEmail());
        Assert.assertEquals("a@sohu.com", result);
    }
}
