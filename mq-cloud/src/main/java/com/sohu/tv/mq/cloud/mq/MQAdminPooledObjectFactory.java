package com.sohu.tv.mq.cloud.mq;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQAdmin对象池
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月3日
 */
public class MQAdminPooledObjectFactory implements KeyedPooledObjectFactory<Cluster, MQAdminExt> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ISohuMQAdminFactory sohuMQAdminFactory;

    @Override
    public PooledObject<MQAdminExt> makeObject(Cluster key) throws Exception {
        DefaultPooledObject<MQAdminExt> pooledObject = new DefaultPooledObject<MQAdminExt>(
                sohuMQAdminFactory.getInstance(key));
        logger.info("{}:create object:{}, key:{}", sohuMQAdminFactory, pooledObject.hashCode(), key);
        return pooledObject;
    }

    @Override
    public void destroyObject(Cluster key, PooledObject<MQAdminExt> p) throws Exception {
        MQAdminExt mqAdmin = p.getObject();
        if (mqAdmin != null) {
            try {
                mqAdmin.shutdown();
            } catch (Exception e) {
                logger.warn("{} shutdown object:{} err, key:{}", sohuMQAdminFactory, p.hashCode(), key, e);
            }
        }
        logger.info("{}:destroy object:{} key:{}", sohuMQAdminFactory, p.hashCode(), key);
    }

    @Override
    public boolean validateObject(Cluster key, PooledObject<MQAdminExt> p) {
        SohuMQAdmin mqAdmin = (SohuMQAdmin) p.getObject();
        try {
            boolean isAlive = mqAdmin.isAlive();
            if (!isAlive) {
                logger.warn("{}:object:{} not alive, key:{}", sohuMQAdminFactory, p.hashCode(), key);
            }
            return isAlive;
        } catch (Exception e) {
            logger.warn("{}:validate object:{} err, key:{}", sohuMQAdminFactory, p.hashCode(), key, e);
        }
        return false;
    }

    @Override
    public void activateObject(Cluster key, PooledObject<MQAdminExt> p) throws Exception {

    }

    @Override
    public void passivateObject(Cluster key, PooledObject<MQAdminExt> p) throws Exception {
    }

    public void setSohuMQAdminFactory(ISohuMQAdminFactory sohuMQAdminFactory) {
        this.sohuMQAdminFactory = sohuMQAdminFactory;
    }
}
