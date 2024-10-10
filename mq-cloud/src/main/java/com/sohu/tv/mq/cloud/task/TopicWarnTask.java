package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.service.TopicWarnService;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * topic预警任务
 *
 * @author yongfeigao
 * @date 2024年09月06日
 */
public class TopicWarnTask {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private TopicWarnService topicWarnService;

    /**
     * 5分钟流量预警任务
     */
    @Scheduled(cron = "55 */5 * * * *")
    @SchedulerLock(name = "topicWarnTask", lockAtMostFor = 2 * 60 * 1000, lockAtLeastFor = 2 * 60 * 1000)
    public void topicWarnTask() {
        taskExecutor.execute(() -> {
            long start = System.currentTimeMillis();
            int count = topicWarnService.warn5Minute();
            logger.info("topicWarnTask size:{} use:{}ms", count, System.currentTimeMillis() - start);
        });
    }

    /**
     * 日流量预警任务
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @SchedulerLock(name = "topicDayWarnTask", lockAtMostFor = 2 * 60 * 1000, lockAtLeastFor = 2 * 60 * 1000)
    public void topicDayWarnTask() {
        long start = System.currentTimeMillis();
        int count = topicWarnService.warnDay();
        logger.info("topicDayWarnTask size:{} use:{}ms", count, System.currentTimeMillis() - start);
    }
}
