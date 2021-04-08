package com.sohu.tv.mq.cloud.util;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

/**
 * ssh链接池工厂
 * 
 * @author yongfeigao
 * @date 2020年12月1日
 */
public class SSHPooledObjectFactory implements KeyedPooledObjectFactory<String, Connection> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private MQCloudConfigHelper mqCloudConfigHelper;

    @Override
    public PooledObject<Connection> makeObject(String ip) throws Exception {
        DefaultPooledObject<Connection> pooledObject = new DefaultPooledObject<Connection>(getConnection(ip));
        logger.info("create object, key:{}", ip);
        return pooledObject;
    }
    
    /**
     * 获取连接并校验
     * 
     * @param ip
     * @return Connection
     * @throws Exception
     */
    private Connection getConnection(String ip) throws Exception {
        Connection conn = buildConnection(ip);
        // 如果private key不为空，优先使用private key验证
        if (StringUtils.isNotBlank(mqCloudConfigHelper.getPrivateKey())) {
            try {
                if (conn.authenticateWithPublicKey(mqCloudConfigHelper.getServerUser(),
                        mqCloudConfigHelper.getPrivateKey().toCharArray(), mqCloudConfigHelper.getServerPassword())) {
                    return conn;
                }
            } catch (Exception e) {
                logger.error("auth with publickKey err, try to user/passwd auth, serverUser:{}, privateKey:{}",
                        mqCloudConfigHelper.getServerUser(), mqCloudConfigHelper.getPrivateKey(), e);
            }
        }
        conn = buildConnection(ip);
        // 其次使用用户名密码验证
        if (conn.authenticateWithPassword(mqCloudConfigHelper.getServerUser(),
                mqCloudConfigHelper.getServerPassword())) {
            return conn;
        }
        throw new Exception("SSH authentication failed with [userName:" +
                mqCloudConfigHelper.getServerUser() + ", password:" + mqCloudConfigHelper.getServerPassword()
                + "]");
    }
    
    /**
     * 构建链接
     * @param ip
     * @return
     * @throws IOException
     */
    private Connection buildConnection(String ip) throws IOException {
        Connection conn = new Connection(ip, mqCloudConfigHelper.getServerPort());
        conn.connect(null, mqCloudConfigHelper.getServerConnectTimeout(),
                    mqCloudConfigHelper.getServerConnectTimeout());
        return conn;
    }

    @Override
    public void destroyObject(String ip, PooledObject<Connection> p) throws Exception {
        Connection connection = p.getObject();
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                logger.warn("close err, key:{}", ip, e);
            }
        }
        logger.info("destroy object {}", ip);
    }

    @Override
    public boolean validateObject(String ip, PooledObject<Connection> p) {
        Connection connection = p.getObject();
        Session session = null;
        try {
            session = connection.openSession();
        } catch (Exception e) {
            logger.warn("validate object err, key:{}, {}", ip, e.getMessage());
            return false;
        } finally {
            if(session != null) {
                session.close();
            }
        }
        return true;
    }

    @Override
    public void activateObject(String ip,PooledObject<Connection> p) throws Exception {
    }

    @Override
    public void passivateObject(String ip, PooledObject<Connection> p) throws Exception {
    }

    public void setMqCloudConfigHelper(MQCloudConfigHelper mqCloudConfigHelper) {
        this.mqCloudConfigHelper = mqCloudConfigHelper;
    }
}
