package com.sohu.tv.mq.cloud.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.ProducerTotalStat;
import com.sohu.tv.mq.cloud.dao.ProducerTotalStatDao;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 生产者总体统计服务
 * 
 * @author yongfeigao
 * @date 2018年9月12日
 */
@Service
public class ProducerTotalStatService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ProducerTotalStatDao producerTotalStatDao;
    
    /**
     * 保存ProducerTotalStat记录
     * 
     * @param producerTotalStat
     * @return 返回Result
     */
    public Result<Integer> save(ProducerTotalStat producerTotalStat) {
        Integer result = null;
        try {
            result = producerTotalStatDao.insert(producerTotalStat);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key, {}", e.getMessage());
            return Result.getDBErrorResult(e);
        } catch (Exception e) {
            logger.error("insert err, producerTotalStat:{}", producerTotalStat, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 查询记录
     * @param producer
     * @param date
     * @return
     */
    public Result<List<ProducerTotalStat>> query(String producer, Date date) {
        List<ProducerTotalStat> result = null;
        try {
            result = producerTotalStatDao.selectByDate(producer, DateUtil.format(date));
        } catch (Exception e) {
            logger.error("query err, producer:{}, date:{}", producer, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 查询记录
     *
     * @param producer
     * @param date
     * @return
     */
    public Result<List<ProducerTotalStat>> queryByStatTime(String producer, int statTime) {
        List<ProducerTotalStat> result = null;
        try {
            result = producerTotalStatDao.selectByStatTime(producer, statTime);
        } catch (Exception e) {
            logger.error("query err, producer:{}, statTime:{}", producer, statTime, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
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
            result = producerTotalStatDao.delete(DateUtil.format(date));
        } catch (Exception e) {
            logger.error("delete err, date:{}", date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 查询producer是否存在
     * @param producer
     * @param date
     * @return
     */
    public Result<Boolean> query(String producer) {
        Boolean result = false;
        try {
            result = producerTotalStatDao.selectByProducer(producer);
        } catch (Exception e) {
            logger.error("query err, producer:{}", producer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result == null ? false : result);
    }
    
    /**
     * 查询异常记录
     * @param producer
     * @param date
     * @return
     */
    public Result<List<ProducerTotalStat>> queryExceptionList(int dt, String time) {
        List<ProducerTotalStat> result = null;
        try {
            result = producerTotalStatDao.selectExceptionList(dt, time);
        } catch (Exception e) {
            logger.error("queryExceptionList err, dt:{}, time:{}", dt, time, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 根据client和time查询producer
     * @param client
     * @param time
     */
    public Result<List<String>> queryProducerList(String client, Date date) {
        List<String> result = null;
        try {
            result = producerTotalStatDao.selectProducerList(client, DateUtil.format(date));
        } catch (Exception e) {
            logger.error("queryProducerList err, client:{}, date:{}", client, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 查询producer流量
     * @param producer
     * @param time
     * @return
     */
    public Result<Integer> query(String producer, int time) {
        try {
            return Result.getResult(producerTotalStatDao.selectByProducerAndTime(producer, time));
        } catch (Exception e) {
            logger.error("query err, producer:{}, time:{}", producer, time, e);
            return Result.getDBErrorResult(e);
        }
    }
}
