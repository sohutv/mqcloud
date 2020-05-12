package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.CheckStatusEnum;
import com.sohu.tv.mq.cloud.dao.BrokerDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * broker
 * 
 * @author yongfeigao
 * @date 2018年11月14日
 */
@Service
public class BrokerService {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private BrokerDao brokerDao;
    
    /**
     * 保存记录
     * @param broker
     * @return
     */
    public Result<?> save(Broker broker) {
        Integer result = null;
        try {
            result = brokerDao.insert(broker);
        } catch (Exception e) {
            logger.error("insert err, broker:{}", broker, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 查询集群的broker
     * 
     * @return Result<List<Broker>>
     */
    public Result<List<Broker>> query(int cid) {
        List<Broker> result = null;
        try {
            result = brokerDao.selectByClusterId(cid);
            if(result != null && result.size() == 0) {
                result = null;
            }
        } catch (Exception e) {
            logger.error("query cid:{} err", cid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 查询所有的broker
     * 
     * @return Result<List<Broker>>
     */
    public Result<List<Broker>> queryAll() {
        List<Broker> result = null;
        try {
            result = brokerDao.selectAll();
        } catch (Exception e) {
            logger.error("query all err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 删除记录
     * 
     * @return 返回Result
     */
    public Result<?> delete(int cid) {
        Integer result = null;
        try {
            result = brokerDao.delete(cid);
        } catch (Exception e) {
            logger.error("delete err, cid:{}", cid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 刷新记录
     * 
     * @return 返回Result
     */
    public Result<?> refresh(int cid, List<Broker> brokerList) {
        try {
            brokerDao.delete(cid);
            for (Broker broker : brokerList) {
                brokerDao.insert(broker);
            }
        } catch (Exception e) {
            logger.error("refresh err, cid:{}", cid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 更新记录
     * @param cid
     * @param addr
     * @return
     */
    public Result<?> update(int cid, String addr, CheckStatusEnum checkStatusEnum) {
        Integer result = null;
        try {
            result = brokerDao.update(cid, addr, checkStatusEnum.getStatus());
        } catch (Exception e) {
            logger.error("update err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
}
