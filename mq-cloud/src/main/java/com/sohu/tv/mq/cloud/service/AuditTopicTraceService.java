package com.sohu.tv.mq.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.AuditTopicTrace;
import com.sohu.tv.mq.cloud.dao.AuditTopicTraceDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * topic trace审核服务
 * 
 * @author yongfeigao
 * @date 2019年11月18日
 */
@Service
public class AuditTopicTraceService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private AuditTopicTraceDao auditTopicTraceDao;
    
    /**
     * 按照aid查询AuditTopicTraceDao
     * 
     * @param Result<AuditTopicTrace>
     */
    public Result<AuditTopicTrace> queryAuditTopicTrace(long aid) {
        AuditTopicTrace auditTopicTrace = null;
        try {
            auditTopicTrace = auditTopicTraceDao.selectByAid(aid);
        } catch (Exception e) {
            logger.error("queryAuditTopicTrace err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditTopicTrace);
    }
}
