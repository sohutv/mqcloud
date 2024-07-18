package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.DataMigration;
import com.sohu.tv.mq.cloud.bo.PageLog;
import com.sohu.tv.mq.cloud.util.Result;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class DataMigrationServiceTest {

    public static final String SOURCE_IP;
    public static final String DEST_IP;

    static {
        try {
            SOURCE_IP = InetAddress.getByName("ip1").getHostAddress();
            DEST_IP = InetAddress.getByName("ip2").getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private DataMigrationService dataMigrationService;

    @Test
    public void testAddMigration() {
        DataMigration dataMigration = new DataMigration();
        dataMigration.setSourceIp(SOURCE_IP);
        dataMigration.setSourcePath("/opt/mqcloud/broker-a/data");
        dataMigration.setDestIp(DEST_IP);
        dataMigration.setDestPath("broker-a");
        Result<?> result = dataMigrationService.addDataMigration(dataMigration);
        Assert.assertTrue(result.isOK());
    }

    @Test
    public void rerunDataMigration() {
        Result<List<DataMigration>> result = dataMigrationService.queryAllDataMigration();
        if (result.isEmpty()) {
            return;
        }
        DataMigration dataMigration = result.getResult().get(0);
        Result<?> rst = dataMigrationService.rerunDataMigration(dataMigration);
        Assert.assertTrue(rst.isOK());
    }

    @Test
    public void testCheckAllDataMigrationTask() {
        int count = dataMigrationService.checkAllDataMigrationTask();
        Assert.assertTrue(count >= 0);
    }

    @Test
    public void testTailLog() throws InterruptedException {
        Result<List<DataMigration>> result = dataMigrationService.queryAllDataMigration();
        if (result.isEmpty()) {
            return;
        }
        DataMigration dataMigration = result.getResult().get(0);
        int offset = 1;
        int size = 10;
        PageLog pageLog = null;
        do {
            pageLog = dataMigrationService.tailLog(dataMigration.getId(), dataMigration.getSourceIp(), offset, size);
            if (pageLog.getContent() != null) {
                pageLog.getContent().forEach(System.out::println);
                offset = pageLog.getNextOffset();
            }
            TimeUnit.SECONDS.sleep(1);
        } while (pageLog.isMore());
    }
}