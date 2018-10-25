package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Audit.StatusEnum;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.AuditAssociateConsumer;
import com.sohu.tv.mq.cloud.bo.AuditAssociateProducer;
import com.sohu.tv.mq.cloud.bo.AuditConsumer;
import com.sohu.tv.mq.cloud.bo.AuditResetOffset;
import com.sohu.tv.mq.cloud.bo.AuditTopic;
import com.sohu.tv.mq.cloud.bo.AuditTopicUpdate;
import com.sohu.tv.mq.cloud.dao.AuditConsumerDeleteDao;
import com.sohu.tv.mq.cloud.dao.AuditDao;
import com.sohu.tv.mq.cloud.dao.AuditResetOffsetDao;
import com.sohu.tv.mq.cloud.dao.AuditTopicDeleteDao;
import com.sohu.tv.mq.cloud.dao.AuditTopicUpdateDao;
import com.sohu.tv.mq.cloud.dao.AuditUserConsumerDeleteDao;
import com.sohu.tv.mq.cloud.dao.AuditUserProducerDeleteDao;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;

/**
 * audit服务
 * 
 * @Description:
 * @author yumeiwang
 * @date 2018年7月16日
 */
@Service
public class AuditService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditDao auditDao;
    
    @Autowired
    private AuditTopicService auditTopicService;
    
    @Autowired
    private AssociateProducerService associateProducerService;
    
    @Autowired
    private AssociateConsumerService associateConsumerService;
    
    @Autowired
    private AuditConsumerService auditConsumerService;
    
    @Autowired
    private AuditTopicDeleteDao auditTopicDeleteDao;
    
    @Autowired
    private AuditConsumerDeleteDao auditConsumerDeleteDao;
    
    @Autowired
    private AuditResetOffsetDao auditResetOffsetDao;
    
    @Autowired
    private AuditTopicUpdateDao auditTopicUpdateDao;
    
    @Autowired
    private AuditUserProducerDeleteDao auditUserProducerDeleteDao;
    
    @Autowired
    private AuditUserConsumerDeleteDao auditUserConsumerDeleteDao;

    /**
     * 查询列表
     * 
     * @param type
     * @return status
     */
    public Result<List<Audit>> queryAuditList(Audit audit) {
        List<Audit> auditList = null;
        try {
            auditList = auditDao.select(audit);
        } catch (Exception e) {
            logger.error("queryNoAuditList err, type:{}", audit.getType(), e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditList);
    }
    
    /**
     * 保存审核admin信息
     * @param audit
     * @return
     */
    public Result<?> saveAuditAdmin(Audit audit){
        try {
            auditDao.insert(audit);
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
    
    /**
     * 保存审核以及topic信息
     * @param audit
     * @param topicParam
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndTopic(Audit audit, AuditTopic auditTopic){
        Long count = null;
        try {
            count = auditDao.insert(audit);
            //如果保存成功，保存auditTopic
            if(count != null && count > 0) {
                auditTopic.setAid(audit.getId());
                auditTopicService.saveAuditTopic(auditTopic);
            }
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", audit);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getResult(Status.DB_DUPLICATE_KEY).setMessage(auditTopic.getName()+"已存在");
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
    
    /**
     * 保存审核以及consumer信息
     * @param audit
     * @param topicParam
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndConsumer(Audit audit, AuditConsumer auditConsumer){
        Long count = null;
        try {
            count = auditDao.insert(audit);
            if(count != null && count > 0) {
                auditConsumer.setAid(audit.getId());
                auditConsumerService.saveAuditConsumer(auditConsumer);
            }
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", audit);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getResult(Status.DB_DUPLICATE_KEY).setMessage(auditConsumer.getConsumer()+"已存在");
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
    
    /**
     * 保存用户关联的生产者
     * @param audit
     * @param auditAssociateProducer
     * @return
     */
    @Transactional
    public Result<Audit> saveAuditAndAssociateProducer(Audit audit, AuditAssociateProducer auditAssociateProducer) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            //如果保存成功，保存auditAssociateProducer
            if(count != null && count > 0) {
                auditAssociateProducer.setAid(audit.getId());
                associateProducerService.save(auditAssociateProducer);
            }
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", audit);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getResult(Status.DB_DUPLICATE_KEY);
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
    
    /**
     * 保存用户关联的消费者
     * @param audit
     * @param AuditAssociateConsumer
     * @return
     */
    @Transactional
    public Result<Audit> saveAuditAndAssociateConsumer(Audit audit, AuditAssociateConsumer auditAssociateConsumer) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            //如果保存成功，保存auditAssociateProducer
            if(count != null && count > 0) {
                auditAssociateConsumer.setAid(audit.getId());
                associateConsumerService.save(auditAssociateConsumer);
            }
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", audit);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getResult(Status.DB_DUPLICATE_KEY);
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
    
    /**
     * 保存审核以及topic删除信息
     * @param audit
     * @param topicParam
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndTopicDelete(Audit audit, long tid, String topic){
        Long count = null;
        try {
            count = auditDao.insert(audit);
            //如果保存成功，保存auditTopic
            if(count != null && count > 0) {
                auditTopicDeleteDao.insert(audit.getId(), tid, topic);
            }
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", audit);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getResult(Status.DB_DUPLICATE_KEY).setMessage("删除申请已存在");
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
    
    /**
     * 保存审核以及topic更新信息
     * @param audit
     * @param auditTopicUpdate
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndTopicUpdate(Audit audit, AuditTopicUpdate auditTopicUpdate){
        Long count = null;
        try {
            count = auditDao.insert(audit);
            //如果保存成功，保存auditTopicUpdate
            if(count != null && count > 0) {
                auditTopicUpdate.setAid(audit.getId());
                auditTopicUpdateDao.insert(auditTopicUpdate);
            }
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
    
    /**
     * 保存审核以及consumer删除信息
     * @param audit
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndConsumerDelete(Audit audit, long cid, String consumer, String topic){
        Long count = null;
        try {
            count = auditDao.insert(audit);
            if(count != null && count > 0) {
                auditConsumerDeleteDao.insert(audit.getId(), cid, consumer, topic);
            }
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
    
    /**
     * 保存审核以及跳过堆积
     * @param audit
     * @param topicParam
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndSkipAccumulation(Audit audit, AuditResetOffset auditResetOffset){
        Long count = null;
        try {
            count = auditDao.insert(audit);
            if(count != null && count > 0) {
                auditResetOffset.setAid(audit.getId());
                auditResetOffsetDao.insert(auditResetOffset);
            }
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
    
    /**
     * 查询
     * 
     * @param type
     * @return status
     */
    public Result<Audit> queryAudit(long id) {
        Audit audit = null;
        try {
            audit = auditDao.selectById(id);
        } catch (Exception e) {
            logger.error("queryAudit err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }

    /**
     * 按类型跟审核状态查询
     * 
     * @param typeEnum
     * @param statusEnum
     * @return
     */
    public Result<List<Audit>> queryAuditByTypeAndStatus(TypeEnum typeEnum, StatusEnum statusEnum) {
        List<Audit> auditList = null;
        try {
            auditList = auditDao.selectAuditByTypeAndStatus(typeEnum.getType(), statusEnum.getStatus());
        } catch (Exception e) {
            logger.error("queryAuditByTypeAndStatus err, type:{}, status:{}", typeEnum.getType(),
                    statusEnum.getStatus(), e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditList);
    }

    /**
     * 更新
     * 
     * @param type
     * @return status
     */
    public Result<Integer> updateAudit(Audit audit) {
        Integer result = null;;
        try {
            result = auditDao.update(audit);
        } catch (Exception e) {
            logger.error("updateAudit err, audit:{}", audit, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 保存审核以及userProducer删除信息
     * 
     * @param audit
     * @param pid
     * @param producer
     * @param topic
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndUserProducerDelete(Audit audit, long pid, String producer, String topic, long uid) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            if (count != null && count > 0) {
                auditUserProducerDeleteDao.insert(audit.getId(), pid, producer, topic, uid);
            }
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
    
    @Transactional
    public Result<?> saveAuditAndUserConsumerDelete(Audit audit, long ucid, String consumer, String topic, long uid) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            if (count != null && count > 0) {
                auditUserConsumerDeleteDao.insert(audit.getId(), ucid, consumer, topic, uid);
            }
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
}
