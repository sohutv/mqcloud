package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.service.ConsumerService;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 自动订阅消费者清理任务
 *
 * @author yongfeigao
 * @date 2025年12月09日
 */
public class AutoSubscribeConsumerTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConsumerService consumerService;

    @Scheduled(cron = "0 0 5 * * ?")
    @SchedulerLock(name = "consumerDeleteTask", lockAtMostFor = 2 * 60 * 1000, lockAtLeastFor = 2 * 60 * 1000)
    public void consumerDeleteTask() {
        long start = System.currentTimeMillis();
        int count = consumerService.deleteUnusedAutoSubscribeConsumer();
        logger.info("consumerDeleteTask size:{} use:{}ms", count, System.currentTimeMillis() - start);
    }
}
