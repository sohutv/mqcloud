package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.service.ServerDataService;
import com.sohu.tv.mq.cloud.util.Result;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

/**
 * 服务器状态监控任务
 *
 * @author yongfeigao
 * @Description:
 * @date 2018年7月18日
 */
public class ServerStatusTask {
    private static final Logger logger = LoggerFactory.getLogger(ServerStatusTask.class);

    //持久化
    @Autowired
    private ServerDataService serverDataService;

    @Autowired
    private TaskExecutor taskExecutor;

    @Scheduled(cron = "3 */5 * * * *")
    @SchedulerLock(name = "fetchServerStatus", lockAtMostFor = 240000, lockAtLeastFor = 240000)
    public void fetchServerStatus() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                serverDataService.fetchAllServerStatus();
            }
        });
    }

    /**
     * 删除服务器统计数据
     */
    @Scheduled(cron = "0 30 4 * * ?")
    @SchedulerLock(name = "deleteServerStatus", lockAtMostFor = 240000, lockAtLeastFor = 240000)
    public void deleteServerStatus() {
        // 30天以前
        long now = System.currentTimeMillis();
        Date thirtyDaysAgo = new Date(now - 30L * 24 * 60 * 60 * 1000);
        logger.info("deleteServerStatus date:{}", thirtyDaysAgo);
        Result<Integer> result = serverDataService.delete(thirtyDaysAgo);
        if (result.isOK()) {
            logger.info("deleteServerStatus success, rows:{} use:{}ms",
                    result.getResult(), (System.currentTimeMillis() - now));
        } else {
            if (result.getException() != null) {
                logger.error("deleteServerStatus err", result.getException());
            } else {
                logger.info("deleteServerStatus failed");
            }
        }
    }
}
