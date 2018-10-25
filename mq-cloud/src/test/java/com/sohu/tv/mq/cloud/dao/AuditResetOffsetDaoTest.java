package com.sohu.tv.mq.cloud.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.AuditResetOffset;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AuditResetOffsetDaoTest {

    @Autowired
    private AuditResetOffsetDao auditResetoffsetDao;

    @Test
    public void insertAuditOffset() {
        AuditResetOffset auditResetoffset = new AuditResetOffset();
        auditResetoffset.setAid(2);
        auditResetoffset.setTid(2);
        auditResetoffset.setConsumerId(11);
        auditResetoffset.setOffset("1");
        auditResetoffsetDao.insert(auditResetoffset);
    }

}
