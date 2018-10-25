package com.sohu.tv.mq.cloud.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.AuditConsumer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AuditConsumerDaoTest {

    @Autowired
    private AuditConsumerDao auditConsumerDao;

    @Test
    public void insertAuditConsumer() {
        AuditConsumer auditConsumer = new AuditConsumer();
        auditConsumer.setAid(1);
        auditConsumer.setTid(1);
        auditConsumer.setConsumer("c1");
        auditConsumer.setConsumeWay(1);
        auditConsumerDao.insert(auditConsumer);
    }

}
