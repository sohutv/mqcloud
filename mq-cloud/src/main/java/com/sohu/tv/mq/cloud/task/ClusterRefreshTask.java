package com.sohu.tv.mq.cloud.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.service.ClusterService;

/**
 * 集群定时刷新
 * 
 * @author yongfeigao
 * @date 2019年2月20日
 */
@Component
public class ClusterRefreshTask {
    
    @Autowired
    private ClusterService clusterService;
    
    @Scheduled(cron = "24 */3 * * * *")
    public void refreshClusterConfig() {
        clusterService.refresh();
    }
}
