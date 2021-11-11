package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ProducerTotalStat;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AlertServiceTest {

    @Autowired
    private AlertService alertService;
    
    @Test
    public void testMail() {
        boolean result = alertService.sendMail("123", "456");
        Assert.assertTrue(result);
    }
    
    @Test
    public void testSendWarnMail() {
        List<User> users = new ArrayList<>();
        User u = new User();
        u.setEmail("abc@sohu-inc.com");
        u.setReceivePhoneNotice(1);
        u.setMobile("18618267000");
        users.add(u);
        
        List<ProducerTotalStat> totalList = new ArrayList<>();
        
        ProducerTotalStat stat = new ProducerTotalStat();
        stat.setBroker("b1");
        stat.setClient("12345");
        stat.setProducer("producer");
        stat.setCreateTime("1021");
        stat.setCreateDate(20211021);
        totalList.add(stat);
        
        stat = new ProducerTotalStat();
        stat.setBroker("b2");
        stat.setClient("12345");
        stat.setProducer("producer");
        stat.setCreateTime("1022");
        stat.setCreateDate(20211023);
        stat.setException("adfsadfsdafdsasaf");
        totalList.add(stat);
        
        stat = new ProducerTotalStat();
        stat.setBroker("b3");
        stat.setClient("12345");
        stat.setProducer("producer");
        stat.setCreateTime("1022");
        stat.setCreateDate(20211023);
        stat.setException("adfsadfsdafdsasaf");
        totalList.add(stat);
        
        
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("link", "www.baidu.com");
        paramMap.put("resource", "producer1");
        paramMap.put("list", totalList);
        alertService.sendWarn(users, WarnType.PRODUCE_EXCEPTION, paramMap);
    }
}
