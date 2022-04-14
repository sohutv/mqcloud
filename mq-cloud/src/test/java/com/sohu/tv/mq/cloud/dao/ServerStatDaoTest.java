package com.sohu.tv.mq.cloud.dao;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.ServerInfoExt;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ServerStatDaoTest {
    
    @Autowired
    private ServerStatusDao serverStatusDao;

    @Test
    public void testSaveServerInfo() {
        serverStatusDao.saveServerInfo("test.mqcloud.com", "init", -1, null);
    }
    
    @Test
    public void testQueryAllServer() {
        List<ServerInfoExt> queryAllServer = serverStatusDao.queryAllServer(new Date());
        for(ServerInfoExt info : queryAllServer) {
            System.out.println(info.getMachineTypeName());
        }
    }

}
