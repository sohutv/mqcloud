package com.sohu.tv.mq.cloud.util;

import org.junit.Assert;
import org.junit.Test;

import com.sohu.tv.mq.cloud.service.SSHTemplate;
import com.sohu.tv.mq.cloud.service.SSHTemplate.DefaultLineProcessor;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHCallback;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHResult;
import com.sohu.tv.mq.cloud.service.SSHTemplate.SSHSession;
import com.sohu.tv.mq.cloud.task.ServerStatusTask;
import com.sohu.tv.mq.cloud.task.server.data.Server;

public class SSHTemplateTest {

    public static final String IP = "127.0.0.1";
    
    @Test
    public void testExecuteStringSSHCallback() throws SSHException {
        SSHTemplate sshTemplate = new SSHTemplate();
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
        SSHTemplate sshTemplate = new SSHTemplate();
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
