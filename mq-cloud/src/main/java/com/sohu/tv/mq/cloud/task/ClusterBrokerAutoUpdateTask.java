package com.sohu.tv.mq.cloud.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sohu.tv.mq.cloud.service.ClusterBrokerAutoUpdateService;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 集群broker自动更新任务
 *
 * @author yongfeigao
 * @date 2020年2月26日
 */
public class ClusterBrokerAutoUpdateTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private ClusterBrokerAutoUpdateService clusterBrokerAutoUpdateService;

    private ThreadPoolExecutor brokerAutoUpdateThreadPool = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(), 0L, TimeUnit.MILLISECONDS,
            new SynchronousQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("brokerAutoUpdate-%d").setDaemon(true).build());

    /**
     * 自动更新
     */
    @Scheduled(fixedDelay = 5000)
    public void autoUpdate() {
        try {
            brokerAutoUpdateThreadPool.execute(() -> {
                taskExecutor.execute(() -> {
                    try {
                        long start = System.currentTimeMillis();
                        Result<?> updateResult = clusterBrokerAutoUpdateService.autoUpdate();
                        long use = System.currentTimeMillis() - start;
                        if (updateResult.isNotOK()) {
                            logger.error("autoUpdate use:{}ms error:{}", use, updateResult);
                        } else if (use > 1000) {
                            logger.warn("autoUpdate use:{}ms", use);
                        }
                    } catch (Exception e) {
                        logger.error("autoUpdate error", e);
                    }
                });
            });
        } catch (Exception e) {
            logger.warn("autoUpdate thread pool is full:{}", e.toString());
        }
    }
}
