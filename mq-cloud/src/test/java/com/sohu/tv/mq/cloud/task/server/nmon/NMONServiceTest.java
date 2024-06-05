package com.sohu.tv.mq.cloud.task.server.nmon;

import com.sohu.tv.mq.cloud.bo.ServerInfo;
import com.sohu.tv.mq.cloud.task.server.data.OSInfo;
import com.sohu.tv.mq.cloud.util.SSHException;
import org.junit.Assert;
import org.junit.Test;

public class NMONServiceTest {

    public static final String IP = "127.0.0.1";
    
    @Test
    public void testStart() throws SSHException {
        NMONService nmonService = new NMONService();
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setIp(IP);
        OSInfo info = nmonService.start(serverInfo);
        Assert.assertNotNull(info);
    }

}
