package com.sohu.tv.mq.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.AuditTopicUpdate;
import com.sohu.tv.mq.cloud.dao.AuditTopicUpdateDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * topic更新审核dao
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
@Service
public class AuditTopicUpdateService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private AuditTopicUpdateDao auditTopicUpdateDao;
    
    /**
     * 保存
     */
    public void save(AuditTopicUpdate auditTopicUpdate) {
        try {
            auditTopicUpdateDao.insert(auditTopicUpdate);
        } catch (Exception e) {
            logger.error("insert err {}", auditTopicUpdate, e);
            throw e;
        }
    }
    
    /**
     * 按照aid查询AuditTopicUpdate
     * 
     * @param Result<AuditTopicUpdate>
     */
    public Result<AuditTopicUpdate> queryAuditTopicUpdate(long aid) {
        AuditTopicUpdate auditTopicUpdate = null;
        try {
            auditTopicUpdate = auditTopicUpdateDao.selectByAid(aid);
        } catch (Exception e) {
            logger.error("queryAuditTopicUpdate err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditTopicUpdate);
    }
}
