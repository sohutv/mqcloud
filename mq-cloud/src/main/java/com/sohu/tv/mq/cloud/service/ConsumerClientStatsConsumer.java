package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.ConsumerClientMetrics;
import com.sohu.tv.mq.cloud.common.MemoryMQConsumer;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.stats.dto.ConsumerClientStats;
import com.sohu.tv.mq.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 消费者客户端统计消费
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/27
 */
@Component
public class ConsumerClientStatsConsumer implements MemoryMQConsumer<ConsumerClientStats> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConsumerClientMetricsService consumerClientMetricsService;

    @Override
    public void consume(ConsumerClientStats consumerClientStats) throws Exception {
        ConsumerClientMetrics consumerClientMetrics = toConsumerClientMetrics(consumerClientStats);
        Result<?> result = consumerClientMetricsService.save(consumerClientMetrics);
        if (result.isOK()) {
            return;
        }
        if (result.getException() != null) {
            if (!(result.getException() instanceof DuplicateKeyException)
                    && !(result.getException() instanceof DataIntegrityViolationException)) {
                // 数据库错误，可以进行重试
                throw result.getException();
            } else {
                // 数据重复，重试一次(兼容老版本)
                if (consumerClientStats.getClientId() != null && consumerClientStats.getClientId().indexOf("@") == -1) {
                    consumerClientStats.setClientId(consumerClientStats.getClientId() + "@1");
                    consume(consumerClientStats);
                    return;
                }
            }
        }
        logger.error("save consumerClientMetrics:{} err", consumerClientMetrics);
    }

    /**
     * ConsumerClientStats转换为ConsumerClientMetrics
     *
     * @param stats
     * @return
     */
    private ConsumerClientMetrics toConsumerClientMetrics(ConsumerClientStats stats) {
        ConsumerClientMetrics metrics = new ConsumerClientMetrics();
        metrics.setClient(stats.getClientId());
        metrics.setConsumer(stats.getConsumer());
        metrics.setStatTime(stats.getStatTime());
        metrics.setAvg(stats.getStats().getAvgTime());
        metrics.setCount(stats.getStats().getTimes());
        metrics.setMax(stats.getStats().getMaxTime());
        Date now = new Date();
        metrics.setCreateDate(DateUtil.format(now));
        metrics.setCreateTime(DateUtil.getFormat(DateUtil.HHMM).format(now));
        if (stats.getStats().getExceptionMap() != null && stats.getStats().getExceptionMap().size() > 0) {
            metrics.setException(JSONUtil.toJSONString(stats.getStats().getExceptionMap()));
        }
        return metrics;
    }
}
