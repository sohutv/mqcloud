package com.sohu.tv.mq.cloud.conf;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.MBeanExporter;

import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.cache.LocalCache;
import com.sohu.tv.mq.cloud.cache.LocalCacheStats;
import com.sohu.tv.mq.cloud.common.Destroyable;
import com.sohu.tv.mq.cloud.common.MemoryMQ;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.common.service.LoginService;
import com.sohu.tv.mq.cloud.common.service.SmsSender;
import com.sohu.tv.mq.cloud.common.service.impl.AbstractLoginService;
import com.sohu.tv.mq.cloud.common.util.CipherHelper;
import com.sohu.tv.mq.cloud.mq.DefaultSohuMQAdmin;
import com.sohu.tv.mq.cloud.mq.MQAdminPooledObjectFactory;
import com.sohu.tv.mq.cloud.mq.SohuMQAdminFactory;
import com.sohu.tv.mq.cloud.service.ClientStatsConsumer;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.stats.dto.ClientStats;

/**
 * 通用配置
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月13日
 */
@Configuration
public class CommonConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private List<Destroyable> list;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    /**
     * 配置用户缓存
     * 
     * @return
     */
    @Bean
    public LocalCache<User> userLocalCache() {
        LocalCache<User> localCache = new LocalCache<User>();
        localCache.setName("user");
        localCache.setSize(1000);
        localCache.setExpireAfterAccess(3600);
        localCache.init();
        return localCache;
    }

    /**
     * 配置mq缓存
     * 
     * @return
     */
    @Bean
    public LocalCache<Object> mqLocalCache() {
        LocalCache<Object> localCache = new LocalCache<Object>();
        localCache.setName("mq");
        localCache.setSize(1000);
        localCache.setExpireAfterAccess(600);
        localCache.init();
        return localCache;
    }

    /**
     * 配置流量缓存
     * 
     * @return
     */
    @Bean
    public LocalCache<Map<String, BrokerTraffic>> trafficLocalCache() {
        LocalCache<Map<String, BrokerTraffic>> localCache = new LocalCache<Map<String, BrokerTraffic>>();
        localCache.setName("traffic");
        localCache.setSize(5000);
        localCache.setExpireAfterWrite(300);
        localCache.init();
        return localCache;
    }

    /**
     * 配置流量抓取缓存
     * 
     * @return
     */
    @Bean
    public LocalCache<String> fetchLocalCache() {
        LocalCache<String> localCache = new LocalCache<String>();
        localCache.setName("fetch");
        localCache.setSize(1000);
        localCache.setExpireAfterAccess(120);
        localCache.init();
        return localCache;
    }

    /**
     * 暴露mbean供外部监控
     * 
     * @param userLocalCache
     * @return
     */
    @Bean
    @SuppressWarnings("rawtypes")
    public MBeanExporter localCacheMBeanExporter(List<LocalCache<?>> localCacheList) {
        MBeanExporter mbeanExporter = new MBeanExporter();
        Map<String, Object> beans = new HashMap<String, Object>();
        for (LocalCache localCache : localCacheList) {
            beans.put("com.sohu.tv.mq.localcache:name=" + localCache.getName(), new LocalCacheStats(localCache));
        }
        mbeanExporter.setBeans(beans);
        return mbeanExporter;
    }
    
    /**
     * mq admin class
     * 
     * @return
     * @throws Exception
     */
    @Bean
    @Profile({"local", "online"})
    public Class<?> mqAdminClass() throws Exception {
        return DefaultSohuMQAdmin.class;
    }
    
    /**
     * mq admin class
     * 
     * @return
     * @throws Exception
     */
    @Bean
    @SuppressWarnings("unchecked")
    @Profile({"local-sohu", "test-sohu", "test-online-sohu", "online-sohu"})
    public Class<SohuMQAdmin> sohuMQAdminClass() throws Exception {
        return (Class<SohuMQAdmin>) Class.forName("com.sohu.tv.mq.cloud.common.mq.SohuMQAdminExt");
    }

    @Bean
    public GenericKeyedObjectPool<Cluster, MQAdminExt> mqPool(Class<SohuMQAdmin> adminClass) {
        GenericKeyedObjectPoolConfig genericKeyedObjectPoolConfig = new GenericKeyedObjectPoolConfig();
        genericKeyedObjectPoolConfig.setTestWhileIdle(true);
        genericKeyedObjectPoolConfig.setMaxTotalPerKey(1);
        genericKeyedObjectPoolConfig.setMaxIdlePerKey(1);
        genericKeyedObjectPoolConfig.setMinIdlePerKey(1);
        genericKeyedObjectPoolConfig.setMaxWaitMillis(10000);
        genericKeyedObjectPoolConfig.setTimeBetweenEvictionRunsMillis(20000);
        MQAdminPooledObjectFactory mqAdminPooledObjectFactory = new MQAdminPooledObjectFactory();
        SohuMQAdminFactory sohuMQAdminFactory = new SohuMQAdminFactory(mqCloudConfigHelper, adminClass);
        mqAdminPooledObjectFactory.setSohuMQAdminFactory(sohuMQAdminFactory);
        GenericKeyedObjectPool<Cluster, MQAdminExt> genericKeyedObjectPool = new GenericKeyedObjectPool<Cluster, MQAdminExt>(
                mqAdminPooledObjectFactory,
                genericKeyedObjectPoolConfig);
        return genericKeyedObjectPool;
    }

    @Bean
    public MemoryMQ<ClientStats> clientStatsMemoryMQ(ClientStatsConsumer clientStatsConsumer) {
        MemoryMQ<ClientStats> memoryMQ = new MemoryMQ<ClientStats>();
        memoryMQ.setConsumerName("clientStats");
        memoryMQ.setBufferSize(10000);
        memoryMQ.setConsumerThreadNum(2);
        memoryMQ.setMinDealIntervalBufferSize(1);
        memoryMQ.setMinBatchDealSize(3);
        memoryMQ.setReconsume(true);
        memoryMQ.setMemoryMQConsumer(clientStatsConsumer);
        memoryMQ.init();
        return memoryMQ;
    }
    
    @PreDestroy
    public void destroy() {
        Collections.sort(list);
        for (Destroyable destroyable : list) {
            try {
                logger.info("destroy:{}", destroyable);
                destroyable.destroy();
            } catch (Exception e) {
                logger.info("destroy err:{}", destroyable, e);
            }
        }
    }

    /**
     * 初始化密码助手
     * 
     * @return
     * @throws UnsupportedEncodingException
     */
    @Bean
    public CipherHelper cipherHelper() throws UnsupportedEncodingException {
        CipherHelper cipherHelper = new CipherHelper(mqCloudConfigHelper.getCiperKey());
        return cipherHelper;
    }

    /**
     * 登录服务配置
     * 
     * @return
     * @throws Exception
     */
    @Bean
    @Profile({"local-sohu", "test-sohu", "test-online-sohu", "online-sohu"})
    public LoginService sohuLoginService() throws Exception {
        Class<?> clz = Class.forName("com.sohu.tv.mq.cloud.common.service.impl.SohuLoginService");
        AbstractLoginService loginService = (AbstractLoginService) clz.newInstance();
        loginService.setCipherHelper(cipherHelper());
        loginService.setTicketKey(mqCloudConfigHelper.getTicketKey());
        loginService.setOnline(mqCloudConfigHelper.isOnline());
        loginService.init();
        return loginService;
    }
    
    /**
     * 短消息服务配置
     * 
     * @return
     * @throws Exception
     */
    @Bean
    @Profile({"online-sohu"})
    public SmsSender smsSender() throws Exception {
        Class<?> clz = Class.forName("com.sohu.tv.mq.cloud.common.service.impl.DefaultSmsSender");
        return (SmsSender) clz.newInstance();
    }

    /**
     * 登录服务配置
     * 
     * @return
     * @throws UnsupportedEncodingException
     */
    @Bean
    @Profile({"local", "online"})
    public LoginService defaultLoginService() throws UnsupportedEncodingException {
        AbstractLoginService loginService = new com.sohu.tv.mq.cloud.service.impl.DefaultLoginService();
        loginService.setCipherHelper(cipherHelper());
        loginService.setTicketKey(mqCloudConfigHelper.getTicketKey());
        loginService.setOnline(mqCloudConfigHelper.isOnline());
        loginService.init();
        return loginService;
    }
}
