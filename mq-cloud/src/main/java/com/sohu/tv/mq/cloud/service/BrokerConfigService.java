package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.BrokerConfig;
import com.sohu.tv.mq.cloud.dao.BrokerConfigDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * broker配置
 * 
 * @author yongfeigao
 * @date 2020年5月19日
 */
@Service
public class BrokerConfigService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BrokerConfigDao brokerConfigDao;

    /**
     * 保存
     * 
     * @return
     */
    public Result<Integer> save(BrokerConfig brokerConfig) {
        Integer result = null;
        try {
            result = brokerConfigDao.insert(brokerConfig);
        } catch (Exception e) {
            logger.error("save:{}", brokerConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 删除
     * 
     * @return
     */
    public Result<Integer> delete(int id) {
        Integer result = null;
        try {
            result = brokerConfigDao.delete(id);
        } catch (Exception e) {
            logger.error("delete:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 修改
     * 
     * @return
     */
    public Result<Integer> update(BrokerConfig brokerConfig) {
        Integer result = null;
        try {
            result = brokerConfigDao.update(brokerConfig);
        } catch (Exception e) {
            logger.error("update:{}", brokerConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 查询
     * 
     * @return
     */
    public Result<List<BrokerConfig>> query() {
        List<BrokerConfig> list = null;
        try {
            list = brokerConfigDao.selectAll();
        } catch (Exception e) {
            logger.error("query", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
    
    /**
     * 查询
     * 
     * @return
     */
    public Result<List<BrokerConfig>> query(int gid) {
        List<BrokerConfig> list = null;
        try {
            list = brokerConfigDao.select(gid);
        } catch (Exception e) {
            logger.error("query gid:{}", gid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
    
    /**
     * 查询
     * 
     * @return
     */
    public Result<BrokerConfig> queryById(int id) {
        BrokerConfig brokerConfig = null;
        try {
            brokerConfig = brokerConfigDao.selectById(id);
        } catch (Exception e) {
            logger.error("query id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(brokerConfig);
    }
    
    /**
     * 查询
     * 
     * @return
     */
    public Result<List<BrokerConfig>> queryByCid(int cid) {
        List<BrokerConfig> list = null;
        try {
            list = brokerConfigDao.selectByCid(cid);
        } catch (Exception e) {
            logger.error("query cid:{}", cid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
}
