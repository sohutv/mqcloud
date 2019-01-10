package com.sohu.tv.mq.cloud.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ServerInfoExt;
import com.sohu.tv.mq.cloud.util.DateUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ServerStatDaoTest {
    
    @Autowired
    private ServerStatusDao serverStatusDao;

    @Test
    public void testSaveServerInfo() {
        serverStatusDao.saveServerInfo("test.mqcloud.com", "init", -1);
    }
    
    @Test
    public void testQueryAllServer() {
        List<ServerInfoExt> queryAllServer = serverStatusDao.queryAllServer(DateUtil.formatYMDNow());
        for(ServerInfoExt info : queryAllServer) {
            System.out.println(info.getMachineTypeName());
        }
    }

}
