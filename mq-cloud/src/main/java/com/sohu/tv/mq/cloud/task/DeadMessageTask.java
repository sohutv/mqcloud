package com.sohu.tv.mq.cloud.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.service.ConsumerDeadTrafficService;

import net.javacrumbs.shedlock.core.SchedulerLock;
/**
 * 死消息监控任务
 * 
 * @author yongfeigao
 * @date 2020年7月10日
 */
public class DeadMessageTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ConsumerDeadTrafficService consumerDeadTrafficService; 
    
    @Autowired
    private TaskExecutor taskExecutor;
    
    /**
     * 每小时的3分33监控一次
     */
    @Scheduled(cron = "33 03 * * * *")
    @SchedulerLock(name = "deadMessageTask", lockAtMostFor = 180000, lockAtLeastFor = 180000)
    public void deadMessageTask() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                logger.info("monitor start");
                long start = System.currentTimeMillis();
                int size = consumerDeadTrafficService.collectHourTraffic();
                logger.info("monitor, size:{},use:{}ms", size, System.currentTimeMillis() - start);
            }
        });
    }
}
