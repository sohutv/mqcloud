package com.sohu.tv.mq.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohu.tv.mq.cloud.bo.AuditBatchAssociate;
import com.sohu.tv.mq.cloud.dao.AuditBatchAssociateDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 批量关联
 * 
 * @author yongfeigao
 * @date 2020年3月18日
 */
@Service
public class AuditBatchAssociateService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private AuditBatchAssociateDao auditBatchAssociateDao;
    
    /**
     * 保存记录
     * 
     * @return 返回Result
     */
    @Transactional
    public Result<?> save(AuditBatchAssociate auditBatchAssociate) {
        try {
            auditBatchAssociateDao.insert(auditBatchAssociate);
        } catch (Exception e) {
            logger.error("insert err, {}", auditBatchAssociate, e);
            throw e;
        }
        return Result.getOKResult();
    }
    
    /**
     * 查询记录
     * 
     * @return 返回Result<AuditBatchAssociate>
     */
    public Result<AuditBatchAssociate> query(long aid) {
        AuditBatchAssociate auditBatchAssociate = null;
        try {
            auditBatchAssociate = auditBatchAssociateDao.select(aid);
        } catch (Exception e) {
            logger.error("query err, {}", aid, e);
        }
        return Result.getResult(auditBatchAssociate);
    }
}
