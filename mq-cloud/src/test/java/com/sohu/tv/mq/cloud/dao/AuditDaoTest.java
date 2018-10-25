package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Audit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class AuditDaoTest {

    @Autowired
    private AuditDao auditDao;

    @Test
    public void insertAudit() {
        Audit audit = new Audit();
        audit.setUid(1233);
        audit.setType(1);
        auditDao.insert(audit);
    }

    @Test
    public void selectAudit() {
        Audit audit = new Audit();
        audit.setType(1);
        audit.setStatus(-1);
        List<Audit> audits = auditDao.select(audit);
        System.out.println(audits.size());
    }

    @Test
    public void modify() {
        Audit audit = new Audit();
        audit.setInfo("测试333333");
        audit.setUid(1);
        audit.setType(0);
        audit.setId(1L);
        audit.setRefuseReason("change1111");
        auditDao.update(audit);
        System.out.println(audit.getId());
    }
}
