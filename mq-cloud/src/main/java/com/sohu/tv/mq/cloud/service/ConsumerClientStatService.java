package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.ConsumerClientStat;
import com.sohu.tv.mq.cloud.dao.ConsumerClientStatDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 消费者-客户端统计服务
 * @author yongweizhao
 * @create 2019/11/6 16:12
 */
@Service
public class ConsumerClientStatService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConsumerClientStatDao consumerClientStatDao;

    /**
     * 保存consumer-client统计信息
     * @param consumerClientStat
     */
    public Result<Integer> save(ConsumerClientStat consumerClientStat) {
        Integer result = null;
        try {
            result = consumerClientStatDao.saveConsumerClientStat(consumerClientStat);
        } catch (Exception e) {
            logger.error("insert err, consumerClientStat:{}", consumerClientStat, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 根据时间和client查询
     */
    public Result<List<String>> selectByDateAndClient(String client, Date date) {
        List<String> result = null;
        try {
            result = consumerClientStatDao.selectByDateAndClient(client, date);
        } catch (Exception e) {
            logger.error("select consumerClientStat err, date:{},client:{}", date, client, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 删除记录
     * @param
     */
    public Result<Integer> delete(Date date) {
        Integer result = null;
        try {
            result = consumerClientStatDao.delete(date);
        } catch (Exception e) {
            logger.error("delete ConsumerClientStat err, date:{}", date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
}
