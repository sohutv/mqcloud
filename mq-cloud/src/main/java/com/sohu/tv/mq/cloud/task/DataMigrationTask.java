package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.service.DataMigrationService;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 数据迁移任务
 *
 * @author yongfeigao
 * @date 2024年7月04日
 */
public class DataMigrationTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private DataMigrationService dataMigrationService;

    /**
     * 数据迁移任务检查
     */
    @Scheduled(cron = "02 */1 * * * *")
    @SchedulerLock(name = "dataMigrationTask", lockAtMostFor = 60 * 1000, lockAtLeastFor = 60 * 1000)
    public void dataMigrationTask() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                long start = System.currentTimeMillis();
                int count = dataMigrationService.checkAllDataMigrationTask();
                logger.info("dataMigrationTask size:{} use:{}ms", count, System.currentTimeMillis() - start);
            }
        });
    }
}
