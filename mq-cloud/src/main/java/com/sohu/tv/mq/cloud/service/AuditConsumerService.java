package com.sohu.tv.mq.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohu.tv.mq.cloud.bo.AuditConsumer;
import com.sohu.tv.mq.cloud.dao.AuditConsumerDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * Consumer审核服务
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月24日
 */
@Service
public class AuditConsumerService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditConsumerDao auditConsumerDao;

    /**
     * 保存接口
     * @param auditTopic
     * @return
     */
    @Transactional
    public Result<Integer> saveAuditConsumer(AuditConsumer auditConsumer){
        Integer count = null;
        try {
            count = auditConsumerDao.insert(auditConsumer);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", auditConsumer);
            throw e;
        } catch (Exception e) {
            logger.error("insert err, auditConsumer:{}", auditConsumer, e);
            throw e;
        }
        return Result.getResult(count);
    }
    
    
    /**
     * 按照id查询AuditConsumer
     * 
     * @param Result<AuditConsumer>
     */
    public Result<AuditConsumer> queryAuditConsumer(long aid) {
        AuditConsumer auditConsumer = null;
        try {
            auditConsumer = auditConsumerDao.selectByAid(aid);
        } catch (Exception e) {
            logger.error("queryAuditConsumer err, aid:{}", aid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditConsumer);
    }
}
