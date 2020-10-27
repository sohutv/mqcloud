package com.sohu.tv.mq.cloud.conf;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.NameServerService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.task.AlarmConfigTask;
import com.sohu.tv.mq.cloud.task.AutoAuditTask;
import com.sohu.tv.mq.cloud.task.BrokerStoreStatTask;
import com.sohu.tv.mq.cloud.task.ClusterMonitorTask;
import com.sohu.tv.mq.cloud.task.ConsumeFailTask;
import com.sohu.tv.mq.cloud.task.ConsumeFallBehindTask;
import com.sohu.tv.mq.cloud.task.ConsumerStatsTask;
import com.sohu.tv.mq.cloud.task.DeadMessageTask;
import com.sohu.tv.mq.cloud.task.MonitorServiceTask;
import com.sohu.tv.mq.cloud.task.ProducerStatsTask;
import com.sohu.tv.mq.cloud.task.ServerStatusTask;
import com.sohu.tv.mq.cloud.task.ServerWarningTask;
import com.sohu.tv.mq.cloud.task.TrafficAnalysisTask;
import com.sohu.tv.mq.cloud.task.TrafficTask;
import com.sohu.tv.mq.cloud.task.monitor.MonitorService;
import com.sohu.tv.mq.cloud.task.monitor.SohuMonitorListener;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;
import net.javacrumbs.shedlock.spring.ScheduledLockConfiguration;
import net.javacrumbs.shedlock.spring.ScheduledLockConfigurationBuilder;

/**
 * 任务配置
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月26日
 */
@Configuration
public class TaskConfiguration {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private TopicService topicService;
    
    @Autowired
    private ConsumerService consumerService;
    
    @Bean
    @Profile({"online", "online-sohu"})
    public TrafficTask TopicTrafficTask() {
        TrafficTask trafficTask = new TrafficTask();
        return trafficTask;
    }
    
    @Bean
    @Profile({"online", "online-sohu"})
    public ConsumeFailTask consumeFailTask() {
        ConsumeFailTask consumeFailTask = new ConsumeFailTask();
        return consumeFailTask;
    }
    
    @Bean
    @Profile({"online", "online-sohu"})
    public ServerStatusTask serverStatusTask() {
        ServerStatusTask serverStatusTask = new ServerStatusTask();
        return serverStatusTask;
    }
    
    @Bean
    @Profile({"online", "online-sohu"})
    public ConsumerStatsTask consumerStatsTask() {
        ConsumerStatsTask consumerStatsTask = new ConsumerStatsTask();
        return consumerStatsTask;
    }

    @Bean
    @Profile({"online", "online-sohu"})
    public ProducerStatsTask producerStatsTask() {
        ProducerStatsTask producerStatsTask = new ProducerStatsTask();
        return producerStatsTask;
    }
    
    @Bean
    @Profile({"online-sohu"})
    public DeadMessageTask deadMessageTask() {
        DeadMessageTask deadMessageTask = new DeadMessageTask();
        return deadMessageTask;
    }
    
    @Bean
    @Profile({"online", "online-sohu"})
    public MonitorServiceTask monitorServiceTask() {
        MonitorServiceTask monitorServiceTask = new MonitorServiceTask();
        return monitorServiceTask;
    }
    
    @Bean
    @Profile({"online", "online-sohu"})
    public AlarmConfigTask alarmConfigTask() {
        return new AlarmConfigTask();
    }
    
    @Bean
    @Profile({"online", "online-sohu"})
    public ServerWarningTask serverEarlyWarningTask() {
        return new ServerWarningTask();
    }
    
    @Bean
    @Profile({"online", "online-sohu"})
    public ClusterMonitorTask mqClusterStatsMonitorServiceTask() {
        return new ClusterMonitorTask();
    }
    
    @Bean
    @Profile({"online", "online-sohu"})
    public AutoAuditTask autoAuditTask() {
        return new AutoAuditTask();
    }
    
    @Bean
    @Profile({"online", "online-sohu"})
    public List<MonitorService> onlineMonitorServiceList(NameServerService nameServerService, 
            SohuMonitorListener sohuMonitorListener, MQCloudConfigHelper mqCloudConfigHelper){
        return monitorServiceList(true, nameServerService, sohuMonitorListener, mqCloudConfigHelper);
    }
    
    @Bean
    @Profile({"local", "test-sohu", "local-sohu"})
    public List<MonitorService> testMonitorServiceList(NameServerService nameServerService, 
            SohuMonitorListener sohuMonitorListener, MQCloudConfigHelper mqCloudConfigHelper){
        return monitorServiceList(false, nameServerService, sohuMonitorListener, mqCloudConfigHelper);
    }
    
    @Bean
    @Profile({"online-sohu"})
    public BrokerStoreStatTask brokerStoreStatTask() {
        BrokerStoreStatTask brokerStoreStatTask = new BrokerStoreStatTask();
        return brokerStoreStatTask;
    }
    
    @Bean
    @Profile({"online-sohu"})
    public ConsumeFallBehindTask consumeFallBehindTask() {
        ConsumeFallBehindTask consumeFallBehindTask = new ConsumeFallBehindTask();
        return consumeFallBehindTask;
    }

    @Bean
    @Profile({"online", "online-sohu"})
    public TrafficAnalysisTask trafficAnalysisTask() {
        return new TrafficAnalysisTask();
    }

    private List<MonitorService> monitorServiceList(boolean online, NameServerService nameServerService, 
            SohuMonitorListener sohuMonitorListener, MQCloudConfigHelper mqCloudConfigHelper){
        if(clusterService.getAllMQCluster() == null) {
            logger.warn("monitorServiceList mqcluster is null");
            return null;
        }
        List<MonitorService> list = new ArrayList<MonitorService>();
        for(Cluster mqCluster : clusterService.getAllMQCluster()) {
            if(online == mqCluster.online()) {
                MonitorService monitorService = new MonitorService(nameServerService, mqCluster, sohuMonitorListener, 
                        mqCloudConfigHelper, topicService);
                monitorService.setConsumerService(consumerService);
                list.add(monitorService);
            }
        }
        return list;
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
