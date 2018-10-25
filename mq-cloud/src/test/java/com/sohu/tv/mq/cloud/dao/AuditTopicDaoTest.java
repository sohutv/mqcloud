package com.sohu.tv.mq.cloud.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.AuditTopic;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AuditTopicDaoTest {

    @Autowired
    private AuditTopicDao auditTopicDao;

    @Test
    public void insertAuditTopic() {
        AuditTopic auditTopic = new AuditTopic();
        auditTopic.setAid(3);
        auditTopic.setName("n3");
        auditTopic.setOrdered(1);
        auditTopic.setQueueNum(8);
        auditTopicDao.insert(auditTopic);
    }
}
