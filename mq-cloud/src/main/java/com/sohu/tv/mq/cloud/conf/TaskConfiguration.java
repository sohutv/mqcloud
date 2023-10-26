package com.sohu.tv.mq.cloud.conf;

import com.sohu.tv.mq.cloud.task.*;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.MQCloudJdbcLockProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 任务配置
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月26日
 */
@Configuration
@ConditionalOnProperty(name = "task.enabled", havingValue = "true")
public class TaskConfiguration {
    
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
    public MonitorServiceTask monitorServiceTask() {
        MonitorServiceTask monitorServiceTask = new MonitorServiceTask();
        return monitorServiceTask;
    }

    @Bean
    public ExportMessageMonitorTask exportMessageMonitorTask() {
        ExportMessageMonitorTask exportMessageMonitorTask = new ExportMessageMonitorTask();
        return exportMessageMonitorTask;
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
    public ConsumerClientMetricsTask consumerClientMetricsTask() {
        return new ConsumerClientMetricsTask();
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
        return new MQCloudJdbcLockProvider(dataSource);
    }
}
