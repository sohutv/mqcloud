package com.sohu.tv.mq.cloud.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.task.monitor.MonitorService;

import net.javacrumbs.shedlock.core.SchedulerLock;
/**
 * 定时监控预警
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月30日
 */
public class MonitorServiceTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private List<MonitorService> sohuMonitorServiceList;
    
    @Autowired
    private TaskExecutor taskExecutor;
    
    /**
     * 每5分钟监控一次
     */
    @Scheduled(cron = "43 */5 * * * *")
    @SchedulerLock(name = "monitor", lockAtMostFor = 180000, lockAtLeastFor = 180000)
    public void monitor() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                if(sohuMonitorServiceList == null) {
                    logger.warn("monitor sohuMonitorServiceList is null");
                    return;
                }
                logger.info("monitor start");
                long start = System.currentTimeMillis();
                for(MonitorService monitorService : sohuMonitorServiceList) {
                    try {
                        monitorService.doMonitorWork();
                    } catch (Exception e) {
                        logger.error("monitor err", e);
                    }
                }
                logger.info("monitor, use:{}ms", System.currentTimeMillis() - start);
            }
        });
    }
    
    /**
     * 每一小时监控一次
     */
    @Scheduled(cron = "23 13 * * * *")
    @SchedulerLock(name = "monitorBroadcast", lockAtMostFor = 180000, lockAtLeastFor = 180000)
    public void monitorBroadcast() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                if(sohuMonitorServiceList == null) {
                    logger.warn("monitorBroadcast is null");
                    return;
                }
                logger.info("monitorBroadcast start");
                long start = System.currentTimeMillis();
                for(MonitorService monitorService : sohuMonitorServiceList) {
                    try {
                        monitorService.monitorBroadCastConsumer();
                    } catch (Exception e) {
                        logger.error("monitorBroadcast err", e);
                    }
                }
                logger.info("monitorBroadcast, use:{}ms", System.currentTimeMillis() - start);
            }
        });
    }
}
