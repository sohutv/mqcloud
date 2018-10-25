package com.sohu.tv.mq.cloud.service;

import java.text.ParseException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.MessageData;
import com.sohu.tv.mq.cloud.bo.MessageQueryCondition;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MessageServiceTest {
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private ClusterService clusterService;

    @Test
    public void test() {
        MessageQueryCondition messageParam = new MessageQueryCondition();
        long start = System.currentTimeMillis() - 10 * 60 * 1000;
        long end = System.currentTimeMillis();
        messageParam.setStart(start);
        messageParam.setEnd(end);
        messageParam.setCid(clusterService.getMQClusterById(2).getId());
        messageParam.setTopic("vrs-topic");
        Result<MessageData> rst =  messageService.queryMessage(messageParam);
        Assert.assertNotNull(rst);
    }

    @Test
    public void test2() throws ParseException {
        MessageQueryCondition messageParam = new MessageQueryCondition();
        Date d1 = DateUtil.parseYMD("20180926");
        Date d2 = DateUtil.getFormat(DateUtil.YMDH).parse("2018092615");
        long start = d1.getTime();
        long end = d2.getTime();
        messageParam.setStart(start);
        messageParam.setEnd(end);
        messageParam.setCid(clusterService.getMQClusterById(3).getId());
        messageParam.setTopic("audit-result-image-sohu-ai-test-topic");
        Result<MessageData> rst =  messageService.queryMessage(messageParam);
        Assert.assertNotNull(rst);
    }
}
