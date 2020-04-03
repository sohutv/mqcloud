package com.sohu.tv.mq.cloud.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.service.AutoAuditService;

import net.javacrumbs.shedlock.core.SchedulerLock;

/**
 * 自动审核任务
 * 
 * @author yongfeigao
 * @date 2020年2月26日
 */
public class AutoAuditTask {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private TaskExecutor taskExecutor;
    
    @Autowired
    private AutoAuditService autoAuditService;
    
    /**
     * 自动审核
     */
    @Scheduled(cron = "33 */2 * * * *")
    @SchedulerLock(name = "autoAuditTask", lockAtMostFor = 120000, lockAtLeastFor = 110000)
    public void autoAudit() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                long start = System.currentTimeMillis();
                int count = autoAuditService.autoAudit();
                logger.info("auto audit size:{} use:{}ms", count, System.currentTimeMillis() - start);
            }
        });
    }
}
