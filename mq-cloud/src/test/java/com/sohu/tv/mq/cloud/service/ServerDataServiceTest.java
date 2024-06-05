package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ServerInfo;
import com.sohu.tv.mq.cloud.bo.ServerInfoExt;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.SSHException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ServerDataServiceTest {

    @Autowired
    private ServerDataService serverDataService;

    @Test
    public void testQueryAllServerStat() {
        List<ServerInfoExt> serverStatList = serverDataService.queryAllServer(new Date());
        System.out.println(serverStatList);
    }

    @Test
    public void testDelete() {
        long now = System.currentTimeMillis();
        Date thirtyDaysAgo = new Date(now - 30L * 24 * 60 * 60 * 1000);
        Result<Integer> result = serverDataService.delete(thirtyDaysAgo);
        System.out.println(result);
    }

    @Test
    public void testFetchServerStatus() throws ParseException, SSHException {
        Date date = DateUtil.getFormat(DateUtil.YMDHM).parse("202405302355");
        List<ServerInfo> serverInfoList = serverDataService.queryAllServerInfo();
        Map<String, List<String>> deployInfo = serverDataService.queryDeployInfo();
        for (ServerInfo server : serverInfoList) {
            server.setDeployDirs(deployInfo.get(server.getIp()));
            server.setCollectTime(date);
            serverDataService.fetchServerStatus(server);
        }
    }

}
