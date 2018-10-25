package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sohu.tv.mq.cloud.bo.AuditAssociateProducer;
import com.sohu.tv.mq.cloud.dao.AuditAssociateProducerDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 审核关联生产者
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月17日
 */
@Service
public class AssociateProducerService {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private AuditAssociateProducerDao auditAssociateProducerDao;
    
    /**
     * 保存AuditAssociateProducer记录
     * 
     * @return 返回Result
     */
    @Transactional
    public Result<?> save(AuditAssociateProducer auditAssociateProducer) {
        try {
            auditAssociateProducerDao.insert(auditAssociateProducer);
        } catch (Exception e) {
            logger.error("insert err, auditAssociateProducer:{}", auditAssociateProducer, e);
            throw e;
        }
        return Result.getOKResult();
    }
    
    /**
     * 查询AuditAssociateProducer记录
     * 
     * @return 返回Result<AuditAssociateProducer>
     */
    public Result<AuditAssociateProducer> query(long aid) {
        AuditAssociateProducer auditAssociateProducer = null;
        try {
            auditAssociateProducer = auditAssociateProducerDao.select(aid);
        } catch (Exception e) {
            logger.error("query err, auditAssociateProducer:{}", auditAssociateProducer, e);
        }
        return Result.getResult(auditAssociateProducer);
    }
    
    /**
     * 查询AuditAssociateProducer记录
     * @param uid
     * @param producer
     * @return
     */
    public Result<List<AuditAssociateProducer>> queryByProducerAndUid(long uid, String producer) {
        List<AuditAssociateProducer> auditAssociateProducer = null;
        try {
            auditAssociateProducer = auditAssociateProducerDao.selectByProducerAndUid(uid, producer);
        } catch (Exception e) {
            logger.error("queryByProducerAndUid err, auditAssociateProducer:{}", auditAssociateProducer, e);
        }
        return Result.getResult(auditAssociateProducer);
    }
    
    /**
     * 查询AuditAssociateProducer记录 不属于该topic的记录
     * @param tid
     * @param producer
     * @return
     */
    public Result<List<AuditAssociateProducer>> queryByProducerAndTid(long tid, String producer) {
        List<AuditAssociateProducer> auditAssociateProducer = null;
        try {
            auditAssociateProducer = auditAssociateProducerDao.selectByProducerAndTid(tid, producer);
        } catch (Exception e) {
            logger.error("queryByProducerAndTid err, auditAssociateProducer:{}", auditAssociateProducer, e);
        }
        return Result.getResult(auditAssociateProducer);
    }
}
