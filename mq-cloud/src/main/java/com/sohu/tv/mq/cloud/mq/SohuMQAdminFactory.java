package com.sohu.tv.mq.cloud.mq;

import java.lang.reflect.Constructor;

import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.remoting.RPCHook;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.util.Constant;

/**
 * sohu mq admin 工厂
 * 
 * @author yongfeigao
 * @date 2020年4月28日
 */
public class SohuMQAdminFactory {
    private MQCloudConfigHelper mqCloudConfigHelper;
    private Class<SohuMQAdmin> sohuMQAdminClass;

    public SohuMQAdminFactory(MQCloudConfigHelper mqCloudConfigHelper, Class<SohuMQAdmin> sohuMQAdminClass) {
        this.mqCloudConfigHelper = mqCloudConfigHelper;
        this.sohuMQAdminClass = sohuMQAdminClass;
    }

    /**
     * 获取启动好的实例
     * 
     * @param key
     * @return
     * @throws Exception
     */
    public SohuMQAdmin getInstance(Cluster key) throws Exception {
        System.setProperty(Constant.ROCKETMQ_NAMESRV_DOMAIN, mqCloudConfigHelper.getDomain());
        SohuMQAdmin sohuMQAdmin = null;
        if (mqCloudConfigHelper.isAdminAclEnable()) {
            SessionCredentials credentials = new SessionCredentials(mqCloudConfigHelper.getAdminAccessKey(),
                    mqCloudConfigHelper.getAdminSecretKey());
            Constructor<SohuMQAdmin> constructor = sohuMQAdminClass.getConstructor(RPCHook.class, long.class);
            sohuMQAdmin = constructor.newInstance(new AclClientRPCHook(credentials), 5000);
        } else {
            sohuMQAdmin = sohuMQAdminClass.newInstance();
        }
        sohuMQAdmin.setVipChannelEnabled(false);
        sohuMQAdmin.setUnitName(String.valueOf(key.getId()));
        sohuMQAdmin.start();
        return sohuMQAdmin;
    }
}
