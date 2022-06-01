package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.service.ClientConnectionService;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: 客户端连接数据解析定时任务
 * @date 2022/4/27 14:58
 */
public class ClientConnectionTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClientConnectionService clientConnectionService;

    @Autowired
    private TaskExecutor taskExecutor;

    /**
     * 采集客户端连接数据 存储客户端语言版本
     * 执行时间 凌晨3点
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @SchedulerLock(name = "collectClientConnection", lockAtMostFor = 180000, lockAtLeastFor = 180000)
    public void scanAllConnection(){
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                long taskStartTime = System.currentTimeMillis();
                clientConnectionService.scanAllClientGroupConnectLanguage(null);
                logger.info("The collect ClientConnection task is completed, and it takes a total of {} milliseconds",
                        System.currentTimeMillis() - taskStartTime);
            }
        });
    }
}
