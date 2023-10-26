package com.sohu.tv.mq.cloud.ssh;

import com.sohu.tv.mq.cloud.Application;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.sshd.client.session.ClientSession;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Auther: yongfeigao
 * @Date: 2023/10/20
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SSHSessionPooledObjectFactoryTest {

    public static final String IP = "test.mqcloud.com";

    @Autowired
    private GenericKeyedObjectPool<String, ClientSession> clientSessionPool;

    @Test
    public void test() throws Exception {
        ClientSession clientSession = null;
        try {
            clientSession = clientSessionPool.borrowObject(IP);
            String rst = clientSession.executeRemoteCommand("date");
            Assert.assertNotNull(rst);
        } finally {
            if (clientSession != null) {
                clientSessionPool.returnObject(IP, clientSession);
            }
        }
    }

    @Test
    public void testMulti() throws Exception {
        for (int i = 0; i < 10; ++i) {
            ClientSession clientSession = null;
            try {
                clientSession = clientSessionPool.borrowObject(IP);
                System.out.println(clientSession);
                String rst = clientSession.executeRemoteCommand("date");
                Assert.assertNotNull(rst);
            } catch (Exception e) {
                System.out.println(e.toString());
            } finally {
                if (clientSession != null) {
                    clientSessionPool.returnObject(IP, clientSession);
                }
            }
        }
    }
}