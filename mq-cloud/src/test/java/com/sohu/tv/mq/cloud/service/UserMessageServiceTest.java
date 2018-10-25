package com.sohu.tv.mq.cloud.service;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.UserMessage;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserMessageServiceTest {
    
    @Autowired
    private UserMessageService userMessageService;

    @Test
    public void testSave() {
        UserMessage userMessage = new UserMessage();
        userMessage.setUid(40);
        userMessage.setMessage("测试一下");
        userMessageService.save(userMessage);
    }

    @Test
    public void testQueryUnread() {
        fail("Not yet implemented");
    }

    @Test
    public void testQueryAll() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetToRead() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetToReadByUid() {
        fail("Not yet implemented");
    }

}
