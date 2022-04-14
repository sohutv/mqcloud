package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.UserMessage;
import com.sohu.tv.mq.cloud.dao.UserMessageDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 用户消息服务
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
@Service
public class UserMessageService {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private UserMessageDao userMessageDao;
    
    /**
     * 保存记录
     * 
     * @param notice
     * @return 返回Result
     */
    public Result<?> save(UserMessage userMessage) {
        try {
            userMessageDao.insert(userMessage);
        } catch (Exception e) {
            logger.error("insert err, userMessage:{}", userMessage, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 查询未读的消息
     * 
     * @return Result<Integer>
     */
    public Result<Integer> queryUnread(long uid) {
        Integer count = null;
        try {
            count = userMessageDao.selectUnreadCount(uid);
        } catch (Exception e) {
            logger.error("queryUnread err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }
    
    /**
     * 置为已读
     * 
     * @return 返回Result
     */
    public Result<?> setToRead(long id, long uid) {
        try {
            userMessageDao.read(id, uid);
        } catch (Exception e) {
            logger.error("update err, id:{}, uid:{}", id, uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 删除notice记录
     * 
     * @return 返回Result
     */
    public Result<?> setToReadByUid(long uid) {
        try {
            userMessageDao.readByUid(uid);
        } catch (Exception e) {
            logger.error("update err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 查询用户消息列表
     * 
     * @param uid
     * @param offset
     * @param size
     * @return
     */
    public Result<List<UserMessage>> queryList(long uid, int offset, int size) {
        List<UserMessage> list = null;
        try {
            list = userMessageDao.select(uid, offset, size);
        } catch (Exception e) {
            logger.error("queryUserWarnList err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 查询用户消息量
     * 
     * @param uid
     * @return
     */
    public Result<Integer> queryCount(long uid) {
        Integer count = null;
        try {
            count = userMessageDao.selectCount(uid);
        } catch (Exception e) {
            logger.error("queryCount err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }
}
