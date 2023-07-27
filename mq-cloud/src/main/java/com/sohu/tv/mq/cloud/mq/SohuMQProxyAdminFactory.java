package com.sohu.tv.mq.cloud.mq;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Proxy;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.service.ProxyService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.util.MQProtocol;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * sohu mq proxy admin 工厂
 *
 * @author yongfeigao
 * @date 2023/6/20
 */
public class SohuMQProxyAdminFactory implements ISohuMQAdminFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private MQCloudConfigHelper mqCloudConfigHelper;

    private ProxyService proxyService;

    private AtomicLong instanceId = new AtomicLong();

    public SohuMQProxyAdminFactory(MQCloudConfigHelper mqCloudConfigHelper, ProxyService proxyService) {
        this.mqCloudConfigHelper = mqCloudConfigHelper;
        this.proxyService = proxyService;
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
        Pair<String, String> proxyAcl = mqCloudConfigHelper.getProxyAcl(key.getId());
        SohuMQAdmin sohuMQAdmin = null;
        if (proxyAcl != null) {
            SessionCredentials credentials = new SessionCredentials(proxyAcl.getObject1(), proxyAcl.getObject2());
            sohuMQAdmin = new DefaultSohuMQAdmin(mqCloudConfigHelper, new AclClientRPCHook(credentials), 5000);
        } else {
            sohuMQAdmin = new DefaultSohuMQAdmin(mqCloudConfigHelper);
        }
        sohuMQAdmin.setAdminExtGroup("proxy_" + sohuMQAdmin.getAdminExtGroup() + instanceId.incrementAndGet());
        sohuMQAdmin.setVipChannelEnabled(false);
        sohuMQAdmin.setUnitName(String.valueOf(key.getId()));
        sohuMQAdmin.setProxyEnabled(true);
        sohuMQAdmin.setProtocolInner(MQProtocol.PROXY_REMOTING.getType());
        sohuMQAdmin.setInstanceName("proxy");
        sohuMQAdmin.start();
        return sohuMQAdmin;
    }
}
