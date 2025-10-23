package com.sohu.tv.mq.cloud.ssh;

import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.sshd.client.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

/**
 * ssh session链接池工厂
 *
 * @Auther: yongfeigao
 * @Date: 2023/10/20
 */
public class SSHSessionPooledObjectFactory implements KeyedPooledObjectFactory<String, ClientSession> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MQCloudConfigHelper mqCloudConfigHelper;

    private SSHClient sshClient;

    public SSHSessionPooledObjectFactory(MQCloudConfigHelper mqCloudConfigHelper) throws GeneralSecurityException, IOException {
        this.mqCloudConfigHelper = mqCloudConfigHelper;
        sshClient = new SSHClient();
        sshClient.setServerUser(mqCloudConfigHelper.getServerUser());
        sshClient.setServerPassword(mqCloudConfigHelper.getServerPassword());
        sshClient.setServerPort(mqCloudConfigHelper.getServerPort());
        sshClient.setServerConnectTimeout(mqCloudConfigHelper.getServerConnectTimeout());
        sshClient.setServerOPTimeout(mqCloudConfigHelper.getServerOPTimeout());
        sshClient.setPrivateKey(mqCloudConfigHelper.getPrivateKey());
        sshClient.init();
    }

    @Override
    public PooledObject<ClientSession> makeObject(String ip) throws Exception {
        ClientSession session = sshClient.getClient().connect(mqCloudConfigHelper.getServerUser(), ip,
                mqCloudConfigHelper.getServerPort()).verify(mqCloudConfigHelper.getServerConnectTimeout(), TimeUnit.MILLISECONDS).getSession();
        session.auth().verify(mqCloudConfigHelper.getServerConnectTimeout(), TimeUnit.MILLISECONDS);
        PooledObject<ClientSession> pooledObject = new DefaultPooledObject<>(session);
        logger.info("create object:{}, key:{}", pooledObject.hashCode(), ip);
        return pooledObject;
    }

    @Override
    public void destroyObject(String ip, PooledObject<ClientSession> pooledObject) throws Exception {
        ClientSession clientSession = pooledObject.getObject();
        if (clientSession != null) {
            try {
                clientSession.close();
            } catch (Exception e) {
                logger.warn("close err object:{}, key:{}", pooledObject.hashCode(), ip, e);
            }
        }
        logger.info("destroy object:{} ip:{}", pooledObject.hashCode(), ip);
    }

    @Override
    public boolean validateObject(String ip, PooledObject<ClientSession> pooledObject) {
        boolean closed = pooledObject.getObject().isClosed();
        if (closed) {
            logger.warn("object:{} ip:{} session closed", pooledObject.hashCode(), ip);
            return false;
        }
        return true;
    }

    @Override
    public void activateObject(String ip, PooledObject<ClientSession> pooledObject) throws Exception {

    }

    @Override
    public void passivateObject(String ip, PooledObject<ClientSession> pooledObject) throws Exception {

    }
}
