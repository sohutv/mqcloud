package com.sohu.tv.mq.cloud.util;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.service.SSHTemplate;
import com.sohu.tv.mq.cloud.service.SSHTemplate.DefaultLineProcessor;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHCallback;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHResult;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHSession;
import com.sohu.tv.mq.cloud.task.ServerStatusTask;
import com.sohu.tv.mq.cloud.task.server.data.Server;

import ch.ethz.ssh2.Connection;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SSHTemplateTest {

    public static final String IP = "127.0.0.1";
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @Autowired
    private SSHTemplate sshTemplate;
    
    @Test
    public void testPublicKey() throws IOException {
        System.out.println(mqCloudConfigHelper.getPrivateKey());
        Connection conn = new Connection(IP, 22);
        conn.connect(null, 5000, 5000);
        boolean isAuthenticated = conn.authenticateWithPublicKey(mqCloudConfigHelper.getServerUser(), mqCloudConfigHelper.getPrivateKey().toCharArray(), mqCloudConfigHelper.getServerPassword());
        Assert.assertTrue(isAuthenticated);
    }
    
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
                SSHResult result = session.executeCommand(ServerStatusTask.COLLECT_SERVER_STATUS, new DefaultLineProcessor() {
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
}
