package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.util.RocketMQVersion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.StoreFiles.StoreFile;
import com.sohu.tv.mq.cloud.bo.StoreFiles.StoreFileType;
import com.sohu.tv.mq.cloud.util.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MQDeployerTest {
    
    @Autowired
    private MQDeployer mqDeployer;

    @Test
    public void testInitConfig() {
        Result<?> rst = mqDeployer.initConfig("127.0.0.1", "ns");
        Assert.assertEquals(true, rst.isOK());
    }

    @Test
    public void testScp() {
        Result<?> rst = mqDeployer.scp("test.mqcloud.com", RocketMQVersion.V4);
        Assert.assertEquals(true, rst.isOK());
    }
    
    @Test
    public void testAuth() {
        String sourceIp = "test.mqcloud.com";
        String destIp = "test2.mqcloud.com";
        Result<?> rst = mqDeployer.authentication(sourceIp, destIp);
        Assert.assertEquals(true, rst.isOK());
    }
    
    @Test
    public void testGetStoreFileList() {
        String ip = "test.mqcloud.com";
        String dir = "broker-a";
        Result<?> rst = mqDeployer.getStoreFileList(ip, dir);
        Assert.assertEquals(true, rst.isOK());
    }
    
    @Test
    public void testCreateStorePath() {
        String ip = "test.mqcloud.com";
        String dir = "broker-a";
        Result<?> rst = mqDeployer.createStorePath(ip, dir);
        Assert.assertEquals(true, rst.isOK());
    }
    
    @Test
    public void testScpStoreFile() {
        String sourceIp = "test.mqcloud.com";
        String sourceHome = "broker-a-s";
        String destIp = "test2.mqcloud.com";
        String destHome = "broker-a";
        StoreFile storeFile = new StoreFile("00000000031138512896", 1073741824, StoreFileType.COMMITLOG);
        Result<?> rst = mqDeployer.scpStoreEntry(sourceIp, sourceHome, destIp, destHome, storeFile);
        Assert.assertEquals(true, rst.isOK());
    }
    
    @Test
    public void testScpStoreFolder() {
        String sourceIp = "test.mqcloud.com";
        String sourceHome = "broker-a-s";
        String destIp = "test2.mqcloud.com";
        String destHome = "broker-a";
        StoreFile storeFile = new StoreFile("vrs-vcms-topic", 1073741824, StoreFileType.CONSUMEQUEUE);
        Result<?> rst = mqDeployer.scpStoreEntry(sourceIp, sourceHome, destIp, destHome, storeFile);
        Assert.assertEquals(true, rst.isOK());
    }

    @Test
    public void testShutdown() {
        String ip = "test.mqcloud.com";
        Result<?> rst = mqDeployer.shutdown(ip, 10911, "broker-log-13");
        Assert.assertEquals(true, rst.isOK());
    }

    @Test
    public void testStartup() {
        String ip = "test.mqcloud.com";
        Result<?> rst = mqDeployer.startup(ip, "/opt/mqcloud/broker-log-13", 10911);
        Assert.assertEquals(true, rst.isOK());
    }
}
