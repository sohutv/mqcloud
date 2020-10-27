package com.sohu.tv.mq.cloud.service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.DecodedMessage;
import com.sohu.tv.mq.cloud.bo.MessageData;
import com.sohu.tv.mq.cloud.bo.MessageQueryCondition;
import com.sohu.tv.mq.cloud.bo.ResentMessageResult;
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
        Result<MessageData> rst =  messageService.queryMessage(messageParam, false);
        Assert.assertNotNull(rst);
    }

    @Test
    public void test2() throws ParseException {
        MessageQueryCondition messageParam = new MessageQueryCondition();
        Date d1 = DateUtil.getFormat(DateUtil.YMDHM).parse("201811211950");
        Date d2 = DateUtil.getFormat(DateUtil.YMDHM).parse("201811212220");
        long start = d1.getTime();
        long end = d2.getTime();
        messageParam.setStart(start);
        messageParam.setEnd(end);
        messageParam.setCid(clusterService.getMQClusterById(1).getId());
        messageParam.setTopic("tv-vrs-datasource-topic");
        messageParam.setKey("109968394");

        do {
            messageParam.prepareForSearch();
            Result<MessageData> rst = messageService.queryMessage(messageParam, false);
            MessageData messageData = rst.getResult();
            messageParam = messageData.getMqc();
            System.out.println("times:" + messageParam.getTimes() + " curSize:" + messageParam.getCurSize()
            + " searchedSize:" + messageParam.getSearchedSize() + " leftSize:" + messageParam.getLeftSize());
            List<DecodedMessage> msgList = messageData.getMsgList();
            for (DecodedMessage m : msgList) {
                String bornTime = DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON)
                        .format(new Date(m.getBornTimestamp()));
                String storeTime = DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON)
                        .format(new Date(m.getStoreTimestamp()));
                System.out.println("clientHost:" + m.getBornHostString()
                        + " clientTime:" + bornTime
                        + " storeTime:" + storeTime
                        + " storeHost:" + m.getStoreHost()
                        + " msgId:" + m.getMsgId() 
                        + " msgBody:" + m.getDecodedBody());
            }
        } while(messageParam.getLeftSize() > 0);
    }
    
    @Test
    public void testResendDirectly() {
        String msgId = "0A131F9000002A9F00000001B214B695";
        String consumer = "basic-apitest-topic-broadcast-consumer";
        int clusterId = 3;
        Result<List<ResentMessageResult>> result = messageService.resendDirectly(clusterService.getMQClusterById(clusterId), msgId, consumer);
        Assert.assertTrue(result.isOK());
    }
}
