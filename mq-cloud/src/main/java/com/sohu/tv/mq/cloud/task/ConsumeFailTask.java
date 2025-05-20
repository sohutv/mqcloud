package com.sohu.tv.mq.cloud.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.service.ConsumerRetryTrafficService;

import net.javacrumbs.shedlock.core.SchedulerLock;
/**
 * 消费失败任务
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月10日
 */
public class ConsumeFailTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ConsumerRetryTrafficService consumerRetryTrafficService; 
    
    @Autowired
    private TaskExecutor taskExecutor;
    
    /**
     * 消费失败监控
     */
    @Scheduled(cron = "55 */6 * * * *")
    @SchedulerLock(name = "consumeFailTask", lockAtMostFor = 180000, lockAtLeastFor = 180000)
    public void consumeFailTask() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                long start = System.currentTimeMillis();
                int size = consumerRetryTrafficService.collectHourTraffic();
                logger.info("monitor size:{},use:{}ms", size, System.currentTimeMillis() - start);
            }
        });
    }
}
