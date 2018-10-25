package com.sohu.tv.mq.cloud.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohu.tv.mq.cloud.bo.AuditTopic;
import com.sohu.tv.mq.cloud.dao.AuditTopicDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * audit-topic服务
 * 
 * @Description:
 * @author yumeiwang
 * @date 2018年7月18日
 */
@Service
public class AuditTopicService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditTopicDao auditTopicDao;

    /**
     * 保存接口
     * @param auditTopic
     * @return
     */
    @Transactional
    public Result<?> saveAuditTopic(AuditTopic auditTopic){
        try {
            auditTopicDao.insert(auditTopic);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", auditTopic);
            throw e;
        } catch (Exception e) {
            logger.error("insert err, auditTopic:{}", auditTopic, e);
            throw e;
        }
        return Result.getOKResult();
    }
    
    
    /**
     * 按照id查询topic
     * 
     * @param Result<AuditTopic>
     */
    public Result<AuditTopic> queryAuditTopic(long id) {
        AuditTopic auditTopic = null;
        try {
            auditTopic = auditTopicDao.selectByAid(id);
        } catch (Exception e) {
            logger.error("queryAuditTopic err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditTopic);
    }
}
