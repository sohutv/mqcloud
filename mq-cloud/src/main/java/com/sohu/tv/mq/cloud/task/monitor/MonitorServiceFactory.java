package com.sohu.tv.mq.cloud.task.monitor;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控服务工厂
 *
 * @Auther: yongfeigao
 * @Date: 2023/7/26
 */
@Component
public class MonitorServiceFactory {

    private Map<Cluster, MonitorService> monitorServiceMap = new HashMap<>();

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private SohuMonitorListener sohuMonitorListener;

    public MonitorService getMonitorService(Cluster cluster) {
        return monitorServiceMap.computeIfAbsent(cluster, k -> {
            try {
                MonitorService monitorService = new MonitorService(cluster, sohuMonitorListener);
                monitorService.setConsumerService(consumerService);
                monitorService.setTopicService(topicService);
                monitorService.setMqAdminTemplate(mqAdminTemplate);
                return monitorService;
            } catch (Exception e) {
                return null;
            }
        });
    }
}
