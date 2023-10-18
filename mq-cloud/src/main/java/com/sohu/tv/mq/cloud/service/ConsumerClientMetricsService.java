package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.ConsumerClientMetrics;
import com.sohu.tv.mq.cloud.bo.ProducerTotalStat;
import com.sohu.tv.mq.cloud.dao.ConsumerClientMetricsDao;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 消费者客户端指标
 *
 * @Auther: yongfeigao
 * @Date: 2023/9/27
 */
@Service
public class ConsumerClientMetricsService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConsumerClientMetricsDao consumerClientMetricsDao;

    /**
     * 保存记录
     *
     * @return 返回Result
     */
    public Result<?> save(ConsumerClientMetrics consumerClientMetrics) {
        try {
            consumerClientMetricsDao.insert(consumerClientMetrics);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key, {}", e.getMessage());
            return Result.getDBErrorResult(e);
        } catch (Exception e) {
            logger.error("insert err, {}", consumerClientMetrics, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 查询记录
     */
    public Result<List<ConsumerClientMetrics>> query(String consumer, Date date) {
        try {
            return Result.getResult(consumerClientMetricsDao.selectByDate(consumer, DateUtil.format(date)));
        } catch (Exception e) {
            logger.error("query err, consumer:{}, date:{}", consumer, date, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询记录
     */
    public Result<List<ConsumerClientMetrics>> query(String consumer, int statTime) {
        try {
            return Result.getResult(consumerClientMetricsDao.selectByStatTime(consumer, statTime));
        } catch (Exception e) {
            logger.error("query err, consumer:{}, statTime:{}", consumer, statTime, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 删除记录
     *
     * @param date
     * @return 返回Result
     */
    public Result<Integer> delete(Date date) {
        Integer result = null;
        try {
            result = consumerClientMetricsDao.delete(DateUtil.format(date));
        } catch (Exception e) {
            logger.error("delete err, date:{}", date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
}
