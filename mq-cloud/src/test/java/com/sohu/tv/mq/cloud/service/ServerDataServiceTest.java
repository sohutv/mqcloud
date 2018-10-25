package com.sohu.tv.mq.cloud.service;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ServerInfoExt;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ServerDataServiceTest {
    
    @Autowired
    private ServerDataService serverDataService;

    @Test
    public void testQueryAllServerStat() {
        List<ServerInfoExt> serverStatList = serverDataService.queryAllServer(DateUtil.formatYMDNow());
        System.out.println(serverStatList);
    }
    
    @Test
    public void testDelte() {
        long now = System.currentTimeMillis();
        Date thirtyDaysAgo = new Date(now - 30L * 24 * 60 * 60 * 1000);
        Result<Integer> result = serverDataService.delete(thirtyDaysAgo);
        System.out.println(result);
    }

}
