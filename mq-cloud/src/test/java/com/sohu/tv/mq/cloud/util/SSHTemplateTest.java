package com.sohu.tv.mq.cloud.util;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.service.SSHTemplate;
import com.sohu.tv.mq.cloud.service.SSHTemplate.DefaultLineProcessor;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHCallback;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHResult;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHSession;
import com.sohu.tv.mq.cloud.service.ServerDataService;
import com.sohu.tv.mq.cloud.task.ServerStatusTask;
import com.sohu.tv.mq.cloud.task.server.data.OSInfo;
import com.sohu.tv.mq.cloud.task.server.data.Server;
import com.sohu.tv.mq.cloud.task.server.nmon.NMONFileFinder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SSHTemplateTest {

    public static final String IP = "test.mqcloud.com";
    
    @Autowired
    private SSHTemplate sshTemplate;

    @Autowired
    private NMONFileFinder nmonFileFinder;
    
    @Test
    public void testExecuteStringSSHCallback() throws SSHException {
        SSHResult rst = sshTemplate.execute(IP, new SSHCallback() {
            public SSHResult call(SSHSession session) {
                SSHResult result = session.executeCommand("uname -a");
                return result;
            }
        });
        Assert.assertNotNull(rst);
    }
    
    @Test
    public void test() throws SSHException {
        SSHResult rst = sshTemplate.execute(IP, new SSHCallback() {
            public SSHResult call(SSHSession session) {
                final Server server = new Server();
                server.setIp(IP);
                SSHResult result = session.executeCommand(ServerDataService.COLLECT_SERVER_STATUS, new DefaultLineProcessor() {
                    public void process(String line, int lineNum) throws Exception {
                        server.parse(line, null);
                    }
                });
                System.out.println(server);
                return result;
            }
        });
        System.out.println(rst);
    }

    @Test
    public void testScpToFile() throws SSHException {
        OSInfo osInfo = new OSInfo();
        osInfo.setUname("x86_64 GNU/Linux");
        File nmonFile = nmonFileFinder.getNMONFile(OSFactory.getDefaultOS(osInfo));
        SSHResult result = sshTemplate.execute(IP, new SSHCallback() {
            public SSHResult call(SSHSession session) {
                SSHResult result = session.scpToFile(nmonFile.getAbsolutePath(), "/tmp/nmon2");
                return result;
            }
        });
        Assert.assertNotNull(result);
    }
}
