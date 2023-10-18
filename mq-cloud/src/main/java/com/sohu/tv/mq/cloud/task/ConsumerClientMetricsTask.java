package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.bo.ProducerTotalStat;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

/**
 * consumer client删除任务
 *
 * @author yongfeigao
 * @date 2023/9/27
 */
public class ConsumerClientMetricsTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConsumerClientMetricsService consumerClientMetricsService;

    /**
     * 删除统计表数据
     */
    @Scheduled(cron = "43 03 5 * * ?")
    @SchedulerLock(name = "deleteConsumerClientMetrics", lockAtMostFor = 600000, lockAtLeastFor = 59000)
    public void deleteConsumerClientMetrics() {
        // 10天以前
        long start = System.currentTimeMillis();
        Date daysAgo = new Date(start - 10L * 24 * 60 * 60 * 1000);
        // 删除
        Result<Integer> result = consumerClientMetricsService.delete(daysAgo);
        if (result.isOK()) {
            logger.info("delete {} success, rows:{} use:{}ms", daysAgo,
                    result.getResult(), (System.currentTimeMillis() - start));
            return;
        }
        if (result.getException() != null) {
            logger.error("delete {} err", daysAgo, result.getException());
            return;
        }
        logger.info("delete {} failed", daysAgo);
    }
}
