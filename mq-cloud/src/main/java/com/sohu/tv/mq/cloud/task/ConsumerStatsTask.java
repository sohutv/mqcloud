package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.service.ConsumerClientStatService;
import com.sohu.tv.mq.cloud.util.Result;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

/**
 * consumer统计定时任务
 * @author yongweizhao
 * @create 2019/11/7 15:41
 */
public class ConsumerStatsTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConsumerClientStatService consumerClientStatService;

    /**
     * 定时删除consumer_client_stat表10天前数据
     * 每天凌晨3点3分执行
     */
    @Scheduled(cron = "0 03 3 * * ?")
    @SchedulerLock(name = "deleteConsumerClientStat", lockAtMostFor = 600000, lockAtLeastFor = 59000)
    public void deleteConsumerClientStat() {
        // 10天以前
        long now = System.currentTimeMillis();
        Date daysAgo = new Date(now - 10L * 24 * 60 * 60 * 1000);
        // 删除consumerClientStat
        Result<Integer> result = consumerClientStatService.delete(daysAgo);
        log(result, daysAgo, "consumerClientStat", now);
    }

    /**
     * 删除数据
     */
    private void log(Result<Integer> result, Date date, String flag, long start) {
        if (result.isOK()) {
            logger.info("{}:{}, delete success, rows:{} use:{}ms", flag, date,
                    result.getResult(), (System.currentTimeMillis() - start));
        } else {
            if (result.getException() != null) {
                logger.error("{}:{}, delete err", flag, date, result.getException());
            } else {
                logger.info("{}:{}, delete failed", flag, date);
            }
        }
    }
}
