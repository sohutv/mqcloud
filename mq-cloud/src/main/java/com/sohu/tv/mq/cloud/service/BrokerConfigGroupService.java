package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.BrokerConfigGroup;
import com.sohu.tv.mq.cloud.dao.BrokerConfigGroupDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * broker配置组
 * 
 * @author yongfeigao
 * @date 2020年5月18日
 */
@Service
public class BrokerConfigGroupService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BrokerConfigGroupDao brokerConfigGroupDao;

    /**
     * 保存
     * 
     * @return
     */
    public Result<Integer> save(String group, int order) {
        Integer result = null;
        try {
            result = brokerConfigGroupDao.insert(group, order);
        } catch (Exception e) {
            logger.error("save:{}", group, e);
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
            result = brokerConfigGroupDao.delete(id);
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
    public Result<Integer> update(BrokerConfigGroup brokerConfigGroup) {
        Integer result = null;
        try {
            result = brokerConfigGroupDao.update(brokerConfigGroup);
        } catch (Exception e) {
            logger.error("update:{}", brokerConfigGroup, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 查询
     * 
     * @return
     */
    public Result<List<BrokerConfigGroup>> query() {
        List<BrokerConfigGroup> list = null;
        try {
            list = brokerConfigGroupDao.select();
        } catch (Exception e) {
            logger.error("query", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
}
