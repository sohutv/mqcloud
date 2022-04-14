package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.UserGroup;
import com.sohu.tv.mq.cloud.dao.UserGroupDao;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;

/**
 * 用户组服务
 * 
 * @author yongfeigao
 * @date 2021年12月24日
 */
@Service
public class UserGroupService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserGroupDao userGroupDao;

    /**
     * 插入用户组
     * 
     * @param groupName
     * @return 返回Result
     */
    public Result<?> save(String groupName) {
        try {
            userGroupDao.insert(groupName);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", groupName);
            return Result.getResult(Status.DB_DUPLICATE_KEY);
        } catch (Exception e) {
            logger.error("insert err, groupName:{}", groupName, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 更新用户组
     */
    public Result<Integer> update(UserGroup userGroup) {
        Integer count = null;
        try {
            count = userGroupDao.update(userGroup);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", userGroup);
            return Result.getResult(Status.DB_DUPLICATE_KEY);
        } catch (Exception e) {
            logger.error("update err, userGroup:{}", userGroup, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }
    
    /**
     * 查询记录
     */
    public Result<UserGroup> query(long id) {
        UserGroup userGroup = null;
        try {
            userGroup = userGroupDao.select(id);
        } catch (Exception e) {
            logger.error("query id:{} err", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userGroup);
    }

    /**
     * 查询记录
     */
    public Result<List<UserGroup>> queryAll() {
        List<UserGroup> userGroupList = null;
        try {
            userGroupList = userGroupDao.selectAll();
        } catch (Exception e) {
            logger.error("selectAll err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(userGroupList);
    }
}
