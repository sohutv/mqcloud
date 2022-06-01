package com.sohu.tv.mq.cloud.conf;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.NameServerService;
import com.sohu.tv.mq.cloud.task.*;
import com.sohu.tv.mq.cloud.task.monitor.MonitorService;
import com.sohu.tv.mq.cloud.task.monitor.SohuMonitorListener;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;
import net.javacrumbs.shedlock.spring.ScheduledLockConfiguration;
import net.javacrumbs.shedlock.spring.ScheduledLockConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务配置
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月26日
 */
@Configuration
@ConditionalOnProperty(name = "task.enabled", havingValue = "true")
public class TaskConfiguration {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private ConsumerService consumerService;
    
    @Bean
    public TrafficTask TopicTrafficTask() {
        TrafficTask trafficTask = new TrafficTask();
        return trafficTask;
    }
    
    @Bean
    public ConsumeFailTask consumeFailTask() {
        ConsumeFailTask consumeFailTask = new ConsumeFailTask();
        return consumeFailTask;
    }
    
    @Bean
    public ServerStatusTask serverStatusTask() {
        ServerStatusTask serverStatusTask = new ServerStatusTask();
        return serverStatusTask;
    }
    
    @Bean
    public ConsumerStatsTask consumerStatsTask() {
        ConsumerStatsTask consumerStatsTask = new ConsumerStatsTask();
        return consumerStatsTask;
    }

    @Bean
    public ProducerStatsTask producerStatsTask() {
        ProducerStatsTask producerStatsTask = new ProducerStatsTask();
        return producerStatsTask;
    }

    @Bean
    public ClientConnectionTask clientConnectionTask() {
        ClientConnectionTask clientConnectionTask = new ClientConnectionTask();
        return clientConnectionTask;
    }
    
    @Bean
    public MonitorServiceTask monitorServiceTask(MQCloudConfigHelper mqCloudConfigHelper,
                                                 NameServerService nameServerService,
                                                 SohuMonitorListener sohuMonitorListener) {
        MonitorServiceTask monitorServiceTask = new MonitorServiceTask();
        if(clusterService.getAllMQCluster() == null) {
            logger.warn("monitorServiceList mqcluster is null");
            return monitorServiceTask;
        }
        List<MonitorService> list = new ArrayList<MonitorService>();
        for(Cluster mqCluster : clusterService.getAllMQCluster()) {
            // 测试环境，监控所有的集群；online环境，只监控online集群
            if (!mqCloudConfigHelper.isOnline() || mqCluster.online()) {
                MonitorService monitorService = new MonitorService(nameServerService, mqCluster, sohuMonitorListener,
                        mqCloudConfigHelper);
                monitorService.setConsumerService(consumerService);
                list.add(monitorService);
            }
        }
        monitorServiceTask.setSohuMonitorServiceList(list);
        return monitorServiceTask;
    }
    
    @Bean
    public AlarmConfigTask alarmConfigTask() {
        return new AlarmConfigTask();
    }
    
    @Bean
    public ServerWarningTask serverEarlyWarningTask() {
        return new ServerWarningTask();
    }
    
    @Bean
    public ClusterMonitorTask mqClusterStatsMonitorServiceTask() {
        return new ClusterMonitorTask();
    }
    
    @Bean
    public AutoAuditTask autoAuditTask() {
        return new AutoAuditTask();
    }

    @Bean
    @ConditionalOnProperty(name = "rocketmq.customized", havingValue = "true")
    public DeadMessageTask deadMessageTask() {
        DeadMessageTask deadMessageTask = new DeadMessageTask();
        return deadMessageTask;
    }

    @Bean
    @ConditionalOnProperty(name = "rocketmq.customized", havingValue = "true")
    public BrokerStoreStatTask brokerStoreStatTask() {
        BrokerStoreStatTask brokerStoreStatTask = new BrokerStoreStatTask();
        return brokerStoreStatTask;
    }
    
    @Bean
    @ConditionalOnProperty(name = "rocketmq.customized", havingValue = "true")
    public ConsumeFallBehindTask consumeFallBehindTask() {
        ConsumeFallBehindTask consumeFallBehindTask = new ConsumeFallBehindTask();
        return consumeFallBehindTask;
    }

    @Bean
    public TrafficAnalysisTask trafficAnalysisTask() {
        return new TrafficAnalysisTask();
    }

    /**
     * 使用数据库作为锁源
     * @param dataSource
     * @return
     */
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcLockProvider(dataSource);
    }
    
    /**
     * 任务调度配置
     * @param lockProvider
     * @return
     */
    @Bean
    public ScheduledLockConfiguration taskScheduler(LockProvider lockProvider) {
        return ScheduledLockConfigurationBuilder
            .withLockProvider(lockProvider)
            .withPoolSize(10)
            .withDefaultLockAtMostFor(Duration.ofMinutes(1))
            .build();
    }
}
