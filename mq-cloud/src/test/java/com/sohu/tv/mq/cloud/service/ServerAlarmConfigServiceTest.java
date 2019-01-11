package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ServerAlarmConfig;
import com.sohu.tv.mq.cloud.util.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ServerAlarmConfigServiceTest {

    @Autowired
    private ServerAlarmConfigService serverAlarmConfigService;

    @Test
    public void testUpdate() {
        ServerAlarmConfig config = new ServerAlarmConfig();
        config.setConnect(100);
        config.setCpuUsageRate(1);
        config.setIobusy(1);
        config.setLoad1(45);
        config.setMemoryUsageRate(1);
        config.setIops(45);
        List<String> ipList = new ArrayList<>();
        ipList.add("test.mqcloud.com");
        ipList.add("test1.mqcloud.com");
        ipList.add("test2.mqcloud.com");
        Result<Integer> update = serverAlarmConfigService.update(config, ipList);
        System.out.println(update);
    }
}
