package com.sohu.tv.mq.cloud.mq;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.util.Constant;

/**
 * MQAdmin对象池
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月3日
 */
public class MQAdminPooledObjectFactory implements KeyedPooledObjectFactory<Cluster, MQAdminExt> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private MQCloudConfigHelper mqCloudConfigHelper; 
    
    @Override
    public PooledObject<MQAdminExt> makeObject(Cluster key) throws Exception {
        System.setProperty(Constant.ROCKETMQ_NAMESRV_DOMAIN, mqCloudConfigHelper.getDomain());
        SohuMQAdmin sohuMQAdmin = new SohuMQAdmin();
        sohuMQAdmin.setVipChannelEnabled(false);
        sohuMQAdmin.setUnitName(String.valueOf(key.getId()));
        sohuMQAdmin.start();
        DefaultPooledObject<MQAdminExt> pooledObject = new DefaultPooledObject<MQAdminExt>(sohuMQAdmin);
        logger.info("create object, key:{}", key);
        return pooledObject;
    }

    @Override
    public void destroyObject(Cluster key, PooledObject<MQAdminExt> p) throws Exception {
        MQAdminExt mqAdmin = p.getObject();
        if (mqAdmin != null) {
            try {
                mqAdmin.shutdown();
            } catch (Exception e) {
                logger.warn("shutdown err, key:{}", key, e);
            }
        }
        logger.info("destroy object {}", key);
    }

    @Override
    public boolean validateObject(Cluster key, PooledObject<MQAdminExt> p) {
        MQAdminExt mqAdmin = p.getObject();
        ClusterInfo clusterInfo = null;
        try {
            clusterInfo = mqAdmin.examineBrokerClusterInfo();
        } catch (Exception e) {
            logger.warn("validate object err, key:{}", key, e);
        }
        if (clusterInfo == null) {
            return false;
        }
        if (clusterInfo.getBrokerAddrTable() == null) {
            return false;
        }
        if (clusterInfo.getBrokerAddrTable().size() <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public void activateObject(Cluster key, PooledObject<MQAdminExt> p) throws Exception {

    }

    @Override
    public void passivateObject(Cluster key, PooledObject<MQAdminExt> p) throws Exception {
    }

    public void setMqCloudConfigHelper(MQCloudConfigHelper mqCloudConfigHelper) {
        this.mqCloudConfigHelper = mqCloudConfigHelper;
    }
}
