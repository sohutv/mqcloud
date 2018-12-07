package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohu.tv.mq.cloud.bo.AuditResendMessage;
import com.sohu.tv.mq.cloud.dao.AuditResendMessageDao;
import com.sohu.tv.mq.cloud.util.Result;
/**
 * 消息重发服务
 * 
 * @author yongfeigao
 * @date 2018年12月6日
 */
@Service
public class AuditResendMessageService {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private AuditResendMessageDao auditResendMessageDao;
    
    /**
     * 查询
     * @param aid
     * @return
     */
    public Result<List<AuditResendMessage>> query(long aid) {
        List<AuditResendMessage> list = null;
        try {
            list = auditResendMessageDao.select(aid);
        } catch (Exception e) {
            logger.error("query err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
    
    /**
     * 查询一条
     * @param aid
     * @param msgId
     * @return
     */
    public Result<AuditResendMessage> queryOne(long aid, String msgId) {
        AuditResendMessage auditResendMessage = null;
        try {
            auditResendMessage = auditResendMessageDao.selectOne(aid, msgId);
        } catch (Exception e) {
            logger.error("queryOne err, aid:{}, msgId:{}", aid, msgId, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditResendMessage);
    }
    
    /**
     * 更新
     * @param aid
     * @param msgId
     * @param status
     * @return
     */
    public Result<Integer> update(long aid, String msgId, int status) {
        Integer updatedRows = null;
        try {
            updatedRows = auditResendMessageDao.update(aid, msgId, status);
        } catch (Exception e) {
            logger.error("update err, aid:{} msgId:{} status:{}", aid, msgId, status, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(updatedRows);
    }
    
    /**
     * 保存
     * @param auditResendMessageList
     * @return
     */
    @Transactional
    public Result<Integer> save(List<AuditResendMessage> auditResendMessageList) {
        Integer updatedRows = null;
        try {
            updatedRows = auditResendMessageDao.insert(auditResendMessageList);
        } catch (Exception e) {
            logger.error("insert err, size:{}", auditResendMessageList.size(), e);
            throw e;
        }
        return Result.getResult(updatedRows);
    }
}
