package com.sohu.tv.mq.cloud.conf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.jmx.export.MBeanExporter;

import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.cache.LocalCache;
import com.sohu.tv.mq.cloud.cache.LocalCacheStats;
import com.sohu.tv.mq.cloud.common.Destroyable;
import com.sohu.tv.mq.cloud.common.MemoryMQ;
import com.sohu.tv.mq.cloud.common.service.AlertMessageSender;
import com.sohu.tv.mq.cloud.common.service.LoginService;
import com.sohu.tv.mq.cloud.common.service.impl.AbstractLoginService;
import com.sohu.tv.mq.cloud.common.util.CipherHelper;
import com.sohu.tv.mq.cloud.mq.MQAdminPooledObjectFactory;
import com.sohu.tv.mq.cloud.service.ClientStatsConsumer;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.MessageTypeLoader;
import com.sohu.tv.mq.serializable.MessageSerializer;
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

    @Bean
    public GenericKeyedObjectPool<Cluster, MQAdminExt> mqPool() {
        GenericKeyedObjectPoolConfig genericKeyedObjectPoolConfig = new GenericKeyedObjectPoolConfig();
        genericKeyedObjectPoolConfig.setTestWhileIdle(true);
        genericKeyedObjectPoolConfig.setMaxTotalPerKey(1);
        genericKeyedObjectPoolConfig.setMaxIdlePerKey(1);
        genericKeyedObjectPoolConfig.setMinIdlePerKey(1);
        genericKeyedObjectPoolConfig.setMaxWaitMillis(3000);
        genericKeyedObjectPoolConfig.setTimeBetweenEvictionRunsMillis(5000);
        MQAdminPooledObjectFactory mqAdminPooledObjectFactory = new MQAdminPooledObjectFactory();
        mqAdminPooledObjectFactory.setMqCloudConfigHelper(mqCloudConfigHelper);
        GenericKeyedObjectPool<Cluster, MQAdminExt> genericKeyedObjectPool = new GenericKeyedObjectPool<Cluster, MQAdminExt>(
                mqAdminPooledObjectFactory,
                genericKeyedObjectPoolConfig);
        return genericKeyedObjectPool;
    }

    /**
     * 构建MessageTypeLoader
     * 
     * @return
     * @throws IOException
     */
    @Bean
    public MessageTypeLoader messageTypeLoader() throws IOException {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources("classpath*:msg-type/*.class");
        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        Map<String, URL> classUrlMap = new HashMap<String, URL>();
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                classUrlMap.put(className, resource.getURL());
            }
        }
        return new MessageTypeLoader(classUrlMap);
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
     * 预警bean配置
     * 
     * @return
     */
    @Bean
    @SuppressWarnings("rawtypes")
    public AlertMessageSender alertMessageSender() {
        String clazz = mqCloudConfigHelper.getAlertClass();
        if (clazz == null) {
            return null;
        }
        try {
            Class clz = Class.forName(clazz);
            return (AlertMessageSender) clz.newInstance();
        } catch (Exception e) {
            logger.error("clazz:{} construct err!", clazz, e);
        }
        return null;
    }

    /**
     * 登录服务配置
     * 
     * @return
     */
    @Bean
    @SuppressWarnings("rawtypes")
    public LoginService loginService() {
        String clazz = mqCloudConfigHelper.getLoginClass();
        if (clazz == null) {
            return null;
        }
        try {
            Class clz = Class.forName(clazz);
            AbstractLoginService loginService = (AbstractLoginService) clz.newInstance();
            loginService.setCipherHelper(cipherHelper());
            loginService.setTicketKey(mqCloudConfigHelper.getTicketKey());
            loginService.setOnline(mqCloudConfigHelper.isOnline());
            loginService.init();
            return loginService;
        } catch (Exception e) {
            logger.error("clazz:{} construct err!", clazz, e);
        }
        return null;
    }
    
    /**
     * 消息序列化工具
     * 
     * @return
     */
    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public MessageSerializer<Object> messageSerializer() {
        String clazz = mqCloudConfigHelper.getMessageSerializerClass();
        if (clazz == null) {
            return null;
        }
        try {
            Class clz = Class.forName(clazz);
            return (MessageSerializer<Object>) clz.newInstance();
        } catch (Exception e) {
            logger.error("clazz:{} construct err!", clazz, e);
        }
        return null;
    }
}
