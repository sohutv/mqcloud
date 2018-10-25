package com.sohu.tv.mq.cloud.task.server.nmon;

import org.junit.Test;

import com.sohu.tv.mq.cloud.service.SSHTemplate;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHCallback;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHResult;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHSession;
import com.sohu.tv.mq.cloud.task.server.data.OSInfo;
import com.sohu.tv.mq.cloud.util.SSHException;

public class NMONServiceTest {

    public static final String IP = "127.0.0.1";
    
    @Test
    public void testStart() throws SSHException {
        NMONService nmonService = new NMONService();
        SSHTemplate sshTemplate = new SSHTemplate();
        sshTemplate.execute(IP, new SSHCallback() {
            public SSHResult call(SSHSession session) {
                OSInfo info = nmonService.start(IP, session);
                System.out.println(info);
                return null;
            }
        });
    }

}
