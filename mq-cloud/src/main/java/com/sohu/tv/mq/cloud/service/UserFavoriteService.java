package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.UserFavorite;
import com.sohu.tv.mq.cloud.dao.UserFavoriteDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户收藏
 * @author: yongfeigao
 * @date: 2022/3/21 16:14
 */
@Component
public class UserFavoriteService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserFavoriteDao userFavoriteDao;

    /**
     * 插入
     */
    public Result<?> save(UserFavorite userFavorite) {
        try {
            userFavoriteDao.insert(userFavorite);
        } catch (Exception e) {
            logger.error("insert err, userFavorite:{}", userFavorite, e);
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
            count = userFavoriteDao.selectCount(uid);
        } catch (Exception e) {
            logger.error("selectCount err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 查询列表
     */
    public Result<List<UserFavorite>> queryByPage(long uid, int offset, int size) {
        List<UserFavorite> list = null;
        try {
            list = userFavoriteDao.selectByPage(uid, offset, size);
        } catch (Exception e) {
            logger.error("queryByPage err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 查询
     */
    public Result<UserFavorite> query(long uid, long tid) {
        UserFavorite userFavorite = null;
        try {
            userFavorite = userFavoriteDao.select(uid, tid);
        } catch (Exception e) {
            logger.error("query err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userFavorite);
    }

    /**
     * 查询
     */
    public Result<UserFavorite> queryById(long id) {
        UserFavorite userFavorite = null;
        try {
            userFavorite = userFavoriteDao.selectById(id);
        } catch (Exception e) {
            logger.error("query err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userFavorite);
    }

    /**
     * 删除
     */
    public Result<Integer> delete(long id) {
        Integer count = null;
        try {
            count = userFavoriteDao.deleteById(id);
        } catch (Exception e) {
            logger.error("deleteById err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 删除
     */
    public Result<Integer> deleteByTid(long tid) {
        Integer count = null;
        try {
            count = userFavoriteDao.deleteByTid(tid);
        } catch (Exception e) {
            logger.error("deleteByTid err, tid:{}", tid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }
}
