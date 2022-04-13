package com.sohu.tv.mq.cloud.mq;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.util.Constant;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;

/**
 * sohu mq admin 工厂
 * 
 * @author yongfeigao
 * @date 2020年4月28日
 */
public class SohuMQAdminFactory {
    private MQCloudConfigHelper mqCloudConfigHelper;

    public SohuMQAdminFactory(MQCloudConfigHelper mqCloudConfigHelper) {
        this.mqCloudConfigHelper = mqCloudConfigHelper;
    }

    /**
     * 获取启动好的实例
     * 
     * @param key
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public SohuMQAdmin getInstance(Cluster key) throws Exception {
		System.setProperty(Constant.ROCKETMQ_NAMESRV_DOMAIN, mqCloudConfigHelper.getDomain());
		SohuMQAdmin sohuMQAdmin = null;
		if (mqCloudConfigHelper.isAdminAclEnable()) {
			SessionCredentials credentials = new SessionCredentials(mqCloudConfigHelper.getAdminAccessKey(),
					mqCloudConfigHelper.getAdminSecretKey());
			sohuMQAdmin = new DefaultSohuMQAdmin(new AclClientRPCHook(credentials), 5000);
		} else {
			sohuMQAdmin = new DefaultSohuMQAdmin();
		}
		sohuMQAdmin.setVipChannelEnabled(false);
		sohuMQAdmin.setUnitName(String.valueOf(key.getId()));
		sohuMQAdmin.start();
		return sohuMQAdmin;
	}
}
