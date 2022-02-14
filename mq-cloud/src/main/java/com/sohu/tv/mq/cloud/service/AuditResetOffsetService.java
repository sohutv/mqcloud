package com.sohu.tv.mq.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.AuditResetOffset;
import com.sohu.tv.mq.cloud.dao.AuditResetOffsetDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 重置偏移量审核dao
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月25日
 */
@Service
public class AuditResetOffsetService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private AuditResetOffsetDao auditResetOffsetDao;
    
    /**
     * 保存
     */
    public void save(AuditResetOffset auditResetOffset) {
        try {
            auditResetOffsetDao.insert(auditResetOffset);
        } catch (Exception e) {
            logger.error("insert err {}", auditResetOffset, e);
            throw e;
        }
    }
    
    /**
     * 按照aid查询
     * 
     * @param Result<AuditResetOffset>
     */
    public Result<AuditResetOffset> queryAuditResetOffset(long aid) {
        AuditResetOffset auditResetOffset = null;
        try {
            auditResetOffset = auditResetOffsetDao.selectByAid(aid);
        } catch (Exception e) {
            logger.error("queryAuditResetOffset err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditResetOffset);
    }
}
