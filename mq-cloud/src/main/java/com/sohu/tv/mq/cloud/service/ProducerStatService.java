package com.sohu.tv.mq.cloud.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.ProducerStat;
import com.sohu.tv.mq.cloud.dao.ProducerStatDao;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 生产者统计服务
 * 
 * @author yongfeigao
 * @date 2018年9月12日
 */
@Service
public class ProducerStatService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ProducerStatDao producerStatDao;
    
    /**
     * 保存ProducerStat记录
     * 
     * @param producerStatList
     * @return 返回Result
     */
    public Result<Integer> save(List<ProducerStat> producerStatList) {
        Integer result = null;
        try {
            result = producerStatDao.insert(producerStatList);
        } catch (Exception e) {
            logger.error("insert err, producerStatList:{}", producerStatList, e);
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
    public Result<List<ProducerStat>> query(String producer, Date date) {
        List<ProducerStat> result = null;
        try {
            result = producerStatDao.selectByDate(producer, DateUtil.format(date));
        } catch (Exception e) {
            logger.error("query err, producer:{}, date:{}", producer, date, e);
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
    public Result<List<ProducerStat>> query(long id) {
        List<ProducerStat> result = null;
        try {
            result = producerStatDao.selectById(id);
        } catch (Exception e) {
            logger.error("query err, id:{}", id, e);
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
            result = producerStatDao.delete(DateUtil.format(date));
        } catch (Exception e) {
            logger.error("delete err, date:{}", date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
}
