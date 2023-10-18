package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.ShedLock;
import com.sohu.tv.mq.cloud.dao.ShedLockDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 任务锁服务
 *
 * @author yongfeigao
 * @date 2023/9/21
 */
@Service
public class ShedLockService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ShedLockDao shedLockDao;

    /**
     * 查询所有
     */
    public Result<List<ShedLock>> queryAll() {
        List<ShedLock> list = null;
        try {
            list = shedLockDao.select();
        } catch (Exception e) {
            logger.error("queryAll err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 查询所有
     */
    public Result<ShedLock> queryByName(String name) {
        ShedLock shedLock = null;
        try {
            shedLock = shedLockDao.selectByName(name);
        } catch (Exception e) {
            logger.error("queryByName:{} err", name, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(shedLock);
    }

    /**
     * 保存
     *
     * @return 返回Result
     */
    public Result<?> save(ShedLock shedLock) {
        try {
            shedLockDao.insert(shedLock);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", shedLock);
            return Result.getDBErrorResult(e);
        } catch (Exception e) {
            logger.error("insert err, shedLock:{}", shedLock, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 更新
     *
     * @return 返回Result
     */
    public Result<?> update(String name) {
        try {
            shedLockDao.update(name, new Date());
        } catch (Exception e) {
            logger.error("update err, name:{}", name, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
}
