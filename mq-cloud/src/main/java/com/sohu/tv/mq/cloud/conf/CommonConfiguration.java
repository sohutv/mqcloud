package com.sohu.tv.mq.cloud.conf;

import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.cache.LocalCache;
import com.sohu.tv.mq.cloud.common.MemoryMQ;
import com.sohu.tv.mq.cloud.common.service.LoginService;
import com.sohu.tv.mq.cloud.common.service.MailSender;
import com.sohu.tv.mq.cloud.common.service.SmsSender;
import com.sohu.tv.mq.cloud.common.service.impl.AbstractLoginService;
import com.sohu.tv.mq.cloud.common.util.CipherHelper;
import com.sohu.tv.mq.cloud.mq.MQAdminPooledObjectFactory;
import com.sohu.tv.mq.cloud.mq.SohuMQAdminFactory;
import com.sohu.tv.mq.cloud.mq.SohuMQProxyAdminFactory;
import com.sohu.tv.mq.cloud.service.ClientStatsConsumer;
import com.sohu.tv.mq.cloud.service.ConsumerClientStatsConsumer;
import com.sohu.tv.mq.cloud.service.ProxyService;
import com.sohu.tv.mq.cloud.service.UserWarnService;
import com.sohu.tv.mq.cloud.ssh.SSHSessionPooledObjectFactory;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.stats.dto.ClientStats;
import com.sohu.tv.mq.stats.dto.ConsumerClientStats;
import com.sohu.tv.mq.util.Constant;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.MQCloudJdbcLockProvider;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 通用配置
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月13日
 */
@Configuration
public class CommonConfiguration {

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private ProxyService proxyService;

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
        localCache.setExpireAfterWrite(120);
        localCache.init();
        return localCache;
    }

    @Bean
    public GenericKeyedObjectPool<Cluster, MQAdminExt> mqPool() {
        System.setProperty(Constant.ROCKETMQ_NAMESRV_DOMAIN, mqCloudConfigHelper.getDomain());
        GenericKeyedObjectPoolConfig genericKeyedObjectPoolConfig = new GenericKeyedObjectPoolConfig();
        genericKeyedObjectPoolConfig.setTestWhileIdle(true);
        genericKeyedObjectPoolConfig.setMaxTotalPerKey(5);
        genericKeyedObjectPoolConfig.setMaxIdlePerKey(2);
        genericKeyedObjectPoolConfig.setMinIdlePerKey(1);
        genericKeyedObjectPoolConfig.setMaxWaitMillis(10000);
        genericKeyedObjectPoolConfig.setTimeBetweenEvictionRunsMillis(20000);
        genericKeyedObjectPoolConfig.setJmxEnabled(false);
        MQAdminPooledObjectFactory mqAdminPooledObjectFactory = new MQAdminPooledObjectFactory();
        SohuMQAdminFactory sohuMQAdminFactory = new SohuMQAdminFactory(mqCloudConfigHelper);
        mqAdminPooledObjectFactory.setSohuMQAdminFactory(sohuMQAdminFactory);
        GenericKeyedObjectPool<Cluster, MQAdminExt> genericKeyedObjectPool = new GenericKeyedObjectPool<Cluster, MQAdminExt>(
                mqAdminPooledObjectFactory,
                genericKeyedObjectPoolConfig);
        return genericKeyedObjectPool;
    }

    @Bean
    public GenericKeyedObjectPool<Cluster, MQAdminExt> mqProxyPool() {
        GenericKeyedObjectPoolConfig genericKeyedObjectPoolConfig = new GenericKeyedObjectPoolConfig();
        genericKeyedObjectPoolConfig.setTestWhileIdle(true);
        genericKeyedObjectPoolConfig.setMaxTotalPerKey(2);
        genericKeyedObjectPoolConfig.setMaxIdlePerKey(1);
        genericKeyedObjectPoolConfig.setMinIdlePerKey(1);
        genericKeyedObjectPoolConfig.setMaxWaitMillis(10000);
        genericKeyedObjectPoolConfig.setTimeBetweenEvictionRunsMillis(20000);
        genericKeyedObjectPoolConfig.setJmxEnabled(false);
        MQAdminPooledObjectFactory mqAdminPooledObjectFactory = new MQAdminPooledObjectFactory();
        SohuMQProxyAdminFactory sohuMQAdminFactory = new SohuMQProxyAdminFactory(mqCloudConfigHelper, proxyService);
        mqAdminPooledObjectFactory.setSohuMQAdminFactory(sohuMQAdminFactory);
        GenericKeyedObjectPool<Cluster, MQAdminExt> genericKeyedObjectPool = new GenericKeyedObjectPool<Cluster, MQAdminExt>(
                mqAdminPooledObjectFactory,
                genericKeyedObjectPoolConfig);
        return genericKeyedObjectPool;
    }
    
    /**
     * ssh连接池配置
     * @return
     */
    @Bean
    public GenericKeyedObjectPool<String, ClientSession> clientSessionPool() throws GeneralSecurityException, IOException {
        GenericKeyedObjectPoolConfig genericKeyedObjectPoolConfig = new GenericKeyedObjectPoolConfig();
        genericKeyedObjectPoolConfig.setTestWhileIdle(true);
        genericKeyedObjectPoolConfig.setTestOnReturn(true);
        genericKeyedObjectPoolConfig.setMaxTotalPerKey(5);
        genericKeyedObjectPoolConfig.setMaxIdlePerKey(1);
        genericKeyedObjectPoolConfig.setMinIdlePerKey(1);
        genericKeyedObjectPoolConfig.setMaxWaitMillis(30000);
        genericKeyedObjectPoolConfig.setTimeBetweenEvictionRunsMillis(20000);
        genericKeyedObjectPoolConfig.setJmxEnabled(false);
        SSHSessionPooledObjectFactory factory = new SSHSessionPooledObjectFactory(mqCloudConfigHelper);
        GenericKeyedObjectPool<String, ClientSession> genericKeyedObjectPool = new GenericKeyedObjectPool<>(
                factory,
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

    @Bean
    public MemoryMQ<ConsumerClientStats> consumerClientStatsMemoryMQ(ConsumerClientStatsConsumer consumerClientStatsConsumer) {
        MemoryMQ<ConsumerClientStats> memoryMQ = new MemoryMQ<>();
        memoryMQ.setConsumerName("consumerClientStats");
        memoryMQ.setBufferSize(10000);
        memoryMQ.setConsumerThreadNum(2);
        memoryMQ.setMinDealIntervalBufferSize(1);
        memoryMQ.setMinBatchDealSize(3);
        memoryMQ.setReconsume(true);
        memoryMQ.setMemoryMQConsumer(consumerClientStatsConsumer);
        memoryMQ.init();
        return memoryMQ;
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
    public LoginService loginService() throws Exception {
        Class<?> clz = Class.forName(mqCloudConfigHelper.getLoginClass());
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
     * 邮件服务配置
     */
    @Bean
    public MailSender mailSender() throws Exception {
        Class<?> clz = Class.forName(mqCloudConfigHelper.getMailClass());
        return (MailSender) clz.newInstance();
    }

    /**
     * 用户警告服务
     */
    @Bean
    public UserWarnService userWarnService() throws Exception {
        Class<?> clz = Class.forName(mqCloudConfigHelper.getUserWarnServiceClass());
        return (UserWarnService) clz.newInstance();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        RestTemplate restTemplate =
                restTemplateBuilder.requestFactory(() -> new OkHttp3ClientHttpRequestFactory(new OkHttpClient.Builder()
                        .connectionPool(new ConnectionPool())
                        .connectTimeout(2000, TimeUnit.MILLISECONDS)
                        .readTimeout(1000, TimeUnit.MILLISECONDS)
                        .writeTimeout(1000, TimeUnit.MILLISECONDS).build())).build();
        return restTemplate;
    }

    /**
     * 使用数据库作为锁源
     * @param dataSource
     * @return
     */
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new MQCloudJdbcLockProvider(dataSource);
    }

    /**
     * 定时任务调度器
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("mqcloud-scheduled-task-");
        return scheduler;
    }
}
