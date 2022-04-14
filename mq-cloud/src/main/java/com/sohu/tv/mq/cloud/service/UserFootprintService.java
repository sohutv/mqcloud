package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.UserFootprint;
import com.sohu.tv.mq.cloud.dao.UserFootprintDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户足迹服务
 *
 * @author: yongfeigao
 * @date: 2022/3/9 16:09
 */
@Component
public class UserFootprintService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserFootprintDao userFootprintDao;

    /**
     * 插入
     */
    public Result<?> save(UserFootprint userFootprint) {
        try {
            userFootprintDao.insert(userFootprint);
        } catch (Exception e) {
            logger.error("insert err, userFootprint:{}", userFootprint, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 查询数量
     */
    public Result<Integer> queryCount(long uid) {
        Integer count = null;
        try {
            count = userFootprintDao.selectCount(uid);
        } catch (Exception e) {
            logger.error("selectCount err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 查询列表
     */
    public Result<List<UserFootprint>> queryByPage(long uid, int offset, int size) {
        List<UserFootprint> list = null;
        try {
            list = userFootprintDao.selectByPage(uid, offset, size);
        } catch (Exception e) {
            logger.error("queryByPage err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 删除
     */
    public Result<Integer> deleteByTid(long tid) {
        Integer count = null;
        try {
            count = userFootprintDao.deleteByTid(tid);
        } catch (Exception e) {
            logger.error("deleteByTid err, tid:{}", tid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }
}
