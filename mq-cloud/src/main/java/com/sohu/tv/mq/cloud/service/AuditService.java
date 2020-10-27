package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import com.sohu.tv.mq.cloud.bo.AuditBatchAssociate;
import com.sohu.tv.mq.cloud.bo.AuditConsumer;
import com.sohu.tv.mq.cloud.bo.AuditConsumerConfig;
import com.sohu.tv.mq.cloud.bo.AuditConsumerDelete;
import com.sohu.tv.mq.cloud.bo.AuditResendMessage;
import com.sohu.tv.mq.cloud.bo.AuditResendMessageConsumer;
import com.sohu.tv.mq.cloud.bo.AuditResetOffset;
import com.sohu.tv.mq.cloud.bo.AuditTopic;
import com.sohu.tv.mq.cloud.bo.AuditTopicDelete;
import com.sohu.tv.mq.cloud.bo.AuditTopicTrace;
import com.sohu.tv.mq.cloud.bo.AuditTopicUpdate;
import com.sohu.tv.mq.cloud.bo.AuditUserConsumerDelete;
import com.sohu.tv.mq.cloud.bo.AuditUserProducerDelete;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserConsumer;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.bo.AuditTopicTrafficWarn;
import com.sohu.tv.mq.cloud.dao.AuditConsumerConfigDao;
import com.sohu.tv.mq.cloud.dao.AuditConsumerDeleteDao;
import com.sohu.tv.mq.cloud.dao.AuditDao;
import com.sohu.tv.mq.cloud.dao.AuditResetOffsetDao;
import com.sohu.tv.mq.cloud.dao.AuditTopicDeleteDao;
import com.sohu.tv.mq.cloud.dao.AuditTopicTraceDao;
import com.sohu.tv.mq.cloud.dao.AuditTopicUpdateDao;
import com.sohu.tv.mq.cloud.dao.AuditUserConsumerDeleteDao;
import com.sohu.tv.mq.cloud.dao.AuditUserProducerDeleteDao;
import com.sohu.tv.mq.cloud.dao.UserConsumerDao;
import com.sohu.tv.mq.cloud.dao.UserProducerDao;
import com.sohu.tv.mq.cloud.dao.AuditTopicTrafficWarnDao;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.AuditAssociateConsumerVO;
import com.sohu.tv.mq.cloud.web.vo.AuditAssociateProducerVO;
import com.sohu.tv.mq.cloud.web.vo.AuditConsumerConfigVO;
import com.sohu.tv.mq.cloud.web.vo.AuditConsumerDeleteVO;
import com.sohu.tv.mq.cloud.web.vo.AuditConsumerVO;
import com.sohu.tv.mq.cloud.web.vo.AuditResendMessageVO;
import com.sohu.tv.mq.cloud.web.vo.AuditResetOffsetVO;
import com.sohu.tv.mq.cloud.web.vo.AuditTopicDeleteVO;
import com.sohu.tv.mq.cloud.web.vo.AuditTopicTraceVO;
import com.sohu.tv.mq.cloud.web.vo.AuditTopicUpdateVO;
import com.sohu.tv.mq.cloud.web.vo.AuditUserConsumerDeleteVO;
import com.sohu.tv.mq.cloud.web.vo.AuditUserProducerDeleteVO;
import com.sohu.tv.mq.cloud.web.vo.AuditVO;
import com.sohu.tv.mq.cloud.web.vo.TopicInfoVO;
import com.sohu.tv.mq.cloud.web.vo.UserTopicInfoVO;
import com.sohu.tv.mq.cloud.web.vo.AuditTopicTrafficWarnVO;
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

    @Autowired
    private AuditResendMessageService auditResendMessageService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserProducerService userProducerService;

    @Autowired
    private AuditTopicDeleteService auditTopicDeleteService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditTopicUpdateService auditTopicUpdateService;

    @Autowired
    private AuditConsumerDeleteService auditConsumerDeleteService;

    @Autowired
    private AuditResetOffsetService auditResetOffsetService;

    @Autowired
    private AuditUserProducerDeleteService auditUserProducerDeleteService;

    @Autowired
    private AuditUserConsumerDeleteService auditUserConsumerDeleteService;

    @Autowired
    private UserConsumerService userConsumerService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private AuditTopicTraceDao auditTopicTraceDao;

    @Autowired
    private AuditTopicTraceService auditTopicTraceService;

    @Autowired
    private AuditBatchAssociateService auditBatchAssociateService;

    @Autowired
    private UserProducerDao userProducerDao;

    @Autowired
    private UserConsumerDao userConsumerDao;
    
    @Autowired
    private AuditConsumerConfigService auditConsumerConfigService;
    
    @Autowired
    private AuditConsumerConfigDao auditConsumerConfigDao;

    @Autowired
    private AuditTopicTrafficWarnDao auditTopicTrafficWarnDao;

    @Autowired
    private AuditTopicTrafficWarnService auditTopicTrafficWarnService;

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
     * 
     * @param audit
     * @return
     */
    public Result<?> saveAuditAdmin(Audit audit) {
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
     * 
     * @param audit
     * @param topicParam
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndTopic(Audit audit, AuditTopic auditTopic) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            // 如果保存成功，保存auditTopic
            if (count != null && count > 0) {
                auditTopic.setAid(audit.getId());
                auditTopicService.saveAuditTopic(auditTopic);
            }
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }

    /**
     * 保存审核以及consumer信息
     * 
     * @param audit
     * @param topicParam
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndConsumer(Audit audit, AuditConsumer auditConsumer) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            if (count != null && count > 0) {
                auditConsumer.setAid(audit.getId());
                auditConsumerService.saveAuditConsumer(auditConsumer);
            }
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", audit);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getResult(Status.DB_DUPLICATE_KEY).setMessage(auditConsumer.getConsumer() + "已存在");
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }

    /**
     * 保存用户关联的生产者
     * 
     * @param audit
     * @param auditAssociateProducer
     * @return
     */
    @Transactional
    public Result<Audit> saveAuditAndAssociateProducer(Audit audit, AuditAssociateProducer auditAssociateProducer) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            // 如果保存成功，保存auditAssociateProducer
            if (count != null && count > 0) {
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
     * 
     * @param audit
     * @param AuditAssociateConsumer
     * @return
     */
    @Transactional
    public Result<Audit> saveAuditAndAssociateConsumer(Audit audit, AuditAssociateConsumer auditAssociateConsumer) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            // 如果保存成功，保存auditAssociateProducer
            if (count != null && count > 0) {
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
     * 保存批量关联数据
     * 
     * @param audit
     * @return
     */
    @Transactional
    public Result<Audit> saveAuditAndAssociateBatch(Audit audit, AuditBatchAssociate auditBatchAssociate) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            // 如果保存成功，保存其他的
            if (count != null && count > 0) {
                auditBatchAssociate.setAid(audit.getId());
                auditBatchAssociateService.save(auditBatchAssociate);
            }
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }

    /**
     * 保存审核以及topic删除信息
     * 
     * @param audit
     * @param topicParam
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndTopicDelete(Audit audit, long tid, String topic) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            // 如果保存成功，保存auditTopic
            if (count != null && count > 0) {
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
     * 保存审核以及topic trace信息
     * 
     * @param audit
     * @param topicParam
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndTopicTrace(Audit audit, long tid, int traceEnabled) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            // 如果保存成功，保存auditTopic
            if (count != null && count > 0) {
                auditTopicTraceDao.insert(audit.getId(), tid, traceEnabled);
            }
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }

    /**
     * 保存审核以及topic流量预警信息
     *
     * @param audit
     * @param tid
     * @param trafficWarnEnabled
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndTopicTrafficWarn(Audit audit, long tid, int trafficWarnEnabled) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            // 如果保存成功，保存auditTopic
            if (count != null && count > 0) {
                auditTopicTrafficWarnDao.insert(audit.getId(), tid, trafficWarnEnabled);
            }
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }

    /**
     * 保存审核以及topic更新信息
     * 
     * @param audit
     * @param auditTopicUpdate
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndTopicUpdate(Audit audit, AuditTopicUpdate auditTopicUpdate) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            // 如果保存成功，保存auditTopicUpdate
            if (count != null && count > 0) {
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
     * 
     * @param audit
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndConsumerDelete(Audit audit, long cid, String consumer, String topic) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            if (count != null && count > 0) {
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
     * 
     * @param audit
     * @param topicParam
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndSkipAccumulation(Audit audit, AuditResetOffset auditResetOffset) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            if (count != null && count > 0) {
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
     * 更新
     * 
     * @param type
     * @return status
     */
    public Result<Integer> updateAudit(Audit audit) {
        Integer result = null;
        ;
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

    /**
     * 保存审核以及重发消息信息
     * 
     * @param audit
     * @param auditResendMessageList
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndAuditResendMessage(Audit audit, List<AuditResendMessage> auditResendMessageList,
            AuditResendMessageConsumer auditResendMessageConsumer) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            // 如果保存成功，保存auditResendMessageList
            if (count != null && count > 0) {
                for (AuditResendMessage msg : auditResendMessageList) {
                    msg.setAid(audit.getId());
                }
                auditResendMessageConsumer.setAid(audit.getId());
                auditResendMessageService.save(auditResendMessageList, auditResendMessageConsumer);
            }
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }

    /**
     * 根据uid查询列表
     * 
     * @param type
     * @return status
     */
    public Result<List<Audit>> queryAuditList(long uid) {
        List<Audit> auditList = null;
        try {
            auditList = auditDao.selectByUid(uid);
        } catch (Exception e) {
            logger.error("queryAuditList err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(auditList);
    }

    /**
     * 申请详情
     * 
     * @param aid
     * @param map
     * @return
     */
    public Result<?> detail(TypeEnum typeEnum, long aid) {
        switch (typeEnum) {
            case NEW_TOPIC:
                return auditTopicService.queryAuditTopic(aid);
            case UPDATE_TOPIC:
                return getUpdateTopicResult(aid);
            case DELETE_TOPIC:
                return getDeleteTopicResult(aid);
            case NEW_CONSUMER:
                return getAuditConumerResult(aid);
            case DELETE_CONSUMER:
                return getDeleteConsumerResult(aid);
            case RESET_OFFSET:
            case RESET_OFFSET_TO_MAX:
            case RESET_RETRY_OFFSET:
                return getResetOffsetResult(aid);
            case ASSOCIATE_PRODUCER:
                return getAuditAssociateProducerResult(aid);
            case ASSOCIATE_CONSUMER:
                return getAuditAssociateConsumerResult(aid);
            case BECOME_ADMIN:
                return getBecomAdminResult(aid);
            case DELETE_USERPRODUCER:
                return getDeleteUserProducerResult(aid);
            case DELETE_USERCONSUMER:
                return getDeleteUserConsumerResult(aid);
            case RESEND_MESSAGE:
                return getResendMessageResult(aid);
            case UPDATE_TOPIC_TRACE:
                return getUpdateTopicTraceResult(aid);
            case BATCH_ASSOCIATE:
                return getAuditBatchAssociateResult(aid);
            case PAUSE_CONSUME:
            case RESUME_CONSUME:
                return getConsumerConfig(aid);
            case LIMIT_CONSUME:
                return getConsumerConfig(aid);
            case UPDATE_TOPIC_TRAFFIC_WARN:
                return getUpdateTopicTrafficWarnResult(aid);
        }
        return null;
    }

    /**
     * 获取 topic申请删除 信息
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getDeleteTopicResult(long aid) {
        Result<Audit> auditResult = queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 查询 topic删除审核记录
        Result<AuditTopicDelete> auditTopicDeleteResult = auditTopicDeleteService.queryAuditTopicDelete(aid);
        if (auditTopicDeleteResult.isNotOK()) {
            return auditTopicDeleteResult;
        }
        // 已经同意过的，数据已经删除了
        if (StatusEnum.AGREE.getStatus() == auditResult.getResult().getStatus()) {
            AuditTopicDeleteVO auditTopicDeleteVO = new AuditTopicDeleteVO();
            Topic t = new Topic();
            t.setName(auditTopicDeleteResult.getResult().getTopic());
            auditTopicDeleteVO.setTopic(t);
            auditTopicDeleteVO.setAid(aid);
            return Result.getResult(auditTopicDeleteVO);
        }
        AuditTopicDelete auditTopicDelete = auditTopicDeleteResult.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(auditTopicDelete.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        // 查询该topic生产者记录
        Result<List<UserProducer>> userProducerListResult = userProducerService
                .queryUserProducerByTid(auditTopicDelete.getTid());
        List<UserProducer> userProducerList = null;
        if (userProducerListResult.isNotEmpty()) {
            userProducerList = userProducerListResult.getResult();
            // 查询用户
            List<Long> uidList = new ArrayList<Long>();
            for (UserProducer up : userProducerList) {
                uidList.add(up.getUid());
            }
            Result<List<User>> userListResult = userService.query(uidList);
            if (userListResult.isEmpty()) {
                return userListResult;
            }

            // 赋值用户
            for (UserProducer up : userProducerList) {
                for (User u : userListResult.getResult()) {
                    if (up.getUid() == u.getId()) {
                        if (StringUtils.isBlank(u.getName())) {
                            up.setUsername(u.getEmail());
                        } else {
                            up.setUsername(u.getName());
                        }
                        break;
                    }
                }
            }
        }

        // 组装vo
        AuditTopicDeleteVO auditTopicDeleteVO = new AuditTopicDeleteVO();
        auditTopicDeleteVO.setTopic(topicResult.getResult());
        auditTopicDeleteVO.setUserProducerList(userProducerList);
        auditTopicDeleteVO.setAid(aid);
        return Result.getResult(auditTopicDeleteVO);
    }

    /**
     * 获取审核消费者
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getAuditConumerResult(long aid) {
        Result<AuditConsumer> auditConsumerResult = auditConsumerService.queryAuditConsumer(aid);
        if (auditConsumerResult.isNotOK()) {
            return auditConsumerResult;
        }
        AuditConsumer auditConsumer = auditConsumerResult.getResult();
        Result<Topic> topicResult = topicService.queryTopic(auditConsumer.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        AuditConsumerVO auditConsumerVO = new AuditConsumerVO();
        BeanUtils.copyProperties(auditConsumer, auditConsumerVO);
        auditConsumerVO.setTopic(topicResult.getResult().getName());
        return Result.getResult(auditConsumerVO);
    }

    /**
     * 获取 topic更新 信息
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getUpdateTopicResult(long aid) {
        Result<AuditTopicUpdate> auditTopicUpdateResult = auditTopicUpdateService.queryAuditTopicUpdate(aid);
        if (auditTopicUpdateResult.isNotOK()) {
            return auditTopicUpdateResult;
        }
        AuditTopicUpdate auditTopicUpdate = auditTopicUpdateResult.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(auditTopicUpdate.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }

        // 组装vo
        AuditTopicUpdateVO auditTopicUpdateVO = new AuditTopicUpdateVO();
        BeanUtils.copyProperties(auditTopicUpdate, auditTopicUpdateVO);
        auditTopicUpdateVO.setTopic(topicResult.getResult());
        return Result.getResult(auditTopicUpdateVO);
    }

    /**
     * 获取 topic更新trace信息
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getUpdateTopicTraceResult(long aid) {
        Result<AuditTopicTrace> auditTopicTraceResult = auditTopicTraceService.queryAuditTopicTrace(aid);
        if (auditTopicTraceResult.isNotOK()) {
            return auditTopicTraceResult;
        }
        AuditTopicTrace auditTopicTrace = auditTopicTraceResult.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(auditTopicTrace.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }

        // 组装vo
        AuditTopicTraceVO auditTopicTraceVO = new AuditTopicTraceVO();
        BeanUtils.copyProperties(auditTopicTrace, auditTopicTraceVO);
        auditTopicTraceVO.setTopic(topicResult.getResult());
        return Result.getResult(auditTopicTraceVO);
    }

    /**
     * 获取重发消息详情
     * 
     * @param aid
     * @return
     */
    private Result<?> getResendMessageResult(long aid) {
        // 查询audit
        Result<Audit> auditResult = queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 查询AuditResendMessage
        Audit audit = auditResult.getResult();
        Result<List<AuditResendMessage>> auditResendMessageListResult = auditResendMessageService.query(audit.getId());
        if (auditResendMessageListResult.isEmpty()) {
            return auditResendMessageListResult;
        }

        // 查询topic
        List<AuditResendMessage> auditResendMessageList = auditResendMessageListResult.getResult();
        Result<Topic> topicResult = topicService.queryTopic(auditResendMessageList.get(0).getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }

        // 查询consumer
        Result<Consumer> consumerResult = auditResendMessageService.queryConsumer(aid);
        if (consumerResult.isNotOK() && Status.NO_RESULT.getKey() != consumerResult.getStatus()) {
            return consumerResult;
        }

        // 拼装vo
        AuditResendMessageVO auditResendMessageVO = new AuditResendMessageVO();
        auditResendMessageVO.setTopic(topicResult.getResult().getName());
        auditResendMessageVO.setMsgList(auditResendMessageList);
        if (consumerResult.getResult() != null) {
            auditResendMessageVO.setConsumer(consumerResult.getResult().getName());
        }
        return Result.getResult(auditResendMessageVO);
    }

    /**
     * 获取 consumer申请删除 信息
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getDeleteConsumerResult(long aid) {
        Result<Audit> auditResult = queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 查询consumer删除审核记录
        Result<AuditConsumerDelete> auditConsumerDeleteResult = auditConsumerDeleteService
                .queryAuditConsumerDelete(aid);
        if (auditConsumerDeleteResult.isNotOK()) {
            return auditConsumerDeleteResult;
        }
        // 已经同意过的，数据已经删除了
        if (StatusEnum.AGREE.getStatus() == auditResult.getResult().getStatus()) {
            AuditConsumerDeleteVO auditConsumerDeleteVO = new AuditConsumerDeleteVO();
            Topic topic = new Topic();
            topic.setName(auditConsumerDeleteResult.getResult().getTopic());
            auditConsumerDeleteVO.setTopic(topic);
            Consumer consumer = new Consumer();
            consumer.setName(auditConsumerDeleteResult.getResult().getConsumer());
            auditConsumerDeleteVO.setConsumer(consumer);
            auditConsumerDeleteVO.setAid(aid);
            return Result.getResult(auditConsumerDeleteVO);
        }
        AuditConsumerDelete auditConsumerDelete = auditConsumerDeleteResult.getResult();
        // 查询consumer信息
        Result<Consumer> consumerResult = consumerService.queryById(auditConsumerDelete.getCid());
        if (consumerResult.isNotOK()) {
            return consumerResult;
        }
        Consumer consumer = consumerResult.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(consumer.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        // 查询该消费者的用户
        Result<List<UserConsumer>> userConsumerListResult = userConsumerService
                .queryUserConsumerByConsumerId(consumer.getId());
        if (userConsumerListResult.isNotOK()) {
            return userConsumerListResult;
        }
        // 组装vo
        AuditConsumerDeleteVO auditConsumerDeleteVO = new AuditConsumerDeleteVO();
        auditConsumerDeleteVO.setTopic(topicResult.getResult());
        auditConsumerDeleteVO.setConsumer(consumer);
        auditConsumerDeleteVO.setAid(aid);
        // 修改部分逻辑，让那些无主消费者可以删除（限管理员）
        if (!userConsumerListResult.isEmpty()) {
            List<UserConsumer> userConsumerList = userConsumerListResult.getResult();
            // 查询用户
            List<Long> uidList = new ArrayList<Long>();
            for (UserConsumer uc : userConsumerList) {
                uidList.add(uc.getUid());
            }
            Result<List<User>> userListResult = userService.query(uidList);
            if (userListResult.isEmpty()) {
                return userListResult;
            }
            auditConsumerDeleteVO.setUser(userListResult.getResult());
        }
        return Result.getResult(auditConsumerDeleteVO);
    }

    /**
     * 获取重置偏移量
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getResetOffsetResult(long aid) {
        Result<AuditResetOffset> auditResetOffsetResult = auditResetOffsetService.queryAuditResetOffset(aid);
        if (auditResetOffsetResult.isNotOK()) {
            return auditResetOffsetResult;
        }
        AuditResetOffset auditResetOffset = auditResetOffsetResult.getResult();
        Result<Topic> topicResult = topicService.queryTopic(auditResetOffset.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        Result<Consumer> consumerResult = consumerService.queryById(auditResetOffset.getConsumerId());
        if (consumerResult.isNotOK()) {
            return consumerResult;
        }
        AuditResetOffsetVO auditResetOffsetVO = new AuditResetOffsetVO();
        BeanUtils.copyProperties(auditResetOffset, auditResetOffsetVO);
        auditResetOffsetVO.setTopic(topicResult.getResult().getName());
        auditResetOffsetVO.setConsumer(consumerResult.getResult().getName());
        return Result.getResult(auditResetOffsetVO);
    }

    /**
     * 获取关联生产者
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getAuditAssociateProducerResult(long aid) {
        Result<AuditAssociateProducer> auditAssociateProducerResult = associateProducerService.query(aid);
        if (auditAssociateProducerResult.isNotOK()) {
            return auditAssociateProducerResult;
        }
        AuditAssociateProducer auditAssociateProducer = auditAssociateProducerResult.getResult();
        Result<Topic> topicResult = topicService.queryTopic(auditAssociateProducer.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        // 兼容之前的数据
        long uid = auditAssociateProducer.getUid();
        if (0L == uid) {
            Result<Audit> auditResult = queryAudit(aid);
            if (auditResult.isNotOK()) {
                return auditResult;
            }
            uid = auditResult.getResult().getUid();
        }
        Result<User> userResult = userService.query(uid);
        if (userResult.isNotOK()) {
            return userResult;
        }
        AuditAssociateProducerVO auditAssociateProducerVO = new AuditAssociateProducerVO();
        BeanUtils.copyProperties(auditAssociateProducer, auditAssociateProducerVO);
        auditAssociateProducerVO.setTopic(topicResult.getResult().getName());
        auditAssociateProducerVO
                .setUser(userResult.getResult().getName() == null ? userResult.getResult().getEmailName()
                        : userResult.getResult().getName());
        return Result.getResult(auditAssociateProducerVO);
    }

    /**
     * 获取关联消费者
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getAuditAssociateConsumerResult(long aid) {
        Result<AuditAssociateConsumer> auditAssociateConsumerResult = associateConsumerService.query(aid);
        if (auditAssociateConsumerResult.isNotOK()) {
            return auditAssociateConsumerResult;
        }
        AuditAssociateConsumer auditAssociateConsumer = auditAssociateConsumerResult.getResult();
        Result<Topic> topicResult = topicService.queryTopic(auditAssociateConsumer.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        // 兼容之前的数据
        long uid = auditAssociateConsumer.getUid();
        if (0L == uid) {
            Result<Audit> auditResult = queryAudit(aid);
            if (auditResult.isNotOK()) {
                return auditResult;
            }
            uid = auditResult.getResult().getUid();
        }
        Result<User> userResult = userService.query(uid);
        if (userResult.isNotOK()) {
            return userResult;
        }
        Result<Consumer> consumerResult = consumerService.queryById(auditAssociateConsumer.getCid());
        if (consumerResult.isNotOK()) {
            return consumerResult;
        }
        AuditAssociateConsumerVO auditAssociateConsumerVO = new AuditAssociateConsumerVO();
        BeanUtils.copyProperties(auditAssociateConsumer, auditAssociateConsumerVO);
        auditAssociateConsumerVO.setTopic(topicResult.getResult().getName());
        auditAssociateConsumerVO.setConsumer(consumerResult.getResult().getName());
        auditAssociateConsumerVO
                .setUser(userResult.getResult().getName() == null ? userResult.getResult().getEmailName()
                        : userResult.getResult().getName());
        return Result.getResult(auditAssociateConsumerVO);
    }

    /**
     * 获取批量关联
     * 
     * @param aid
     * @return Result
     */
    public Result<?> getAuditBatchAssociateResult(long aid) {
        Result<AuditBatchAssociate> auditBatchAssociateResult = auditBatchAssociateService.query(aid);
        if (auditBatchAssociateResult.isNotOK()) {
            return auditBatchAssociateResult;
        }
        AuditBatchAssociate auditBatchAssociate = auditBatchAssociateResult.getResult();
        // 获取用户
        List<Long> uidList = auditBatchAssociate.getUidList();
        Result<List<User>> userListResult = userService.query(uidList);
        if (userListResult.isEmpty()) {
            return userListResult;
        }

        // 获取生产者
        List<UserProducer> userProducerList = null;
        List<Long> producerIdList = auditBatchAssociate.getProducerIdList();
        if (producerIdList != null) {
            Result<List<UserProducer>> userProducerListResult = userProducerService.query(producerIdList);
            if (userProducerListResult.isNotEmpty()) {
                userProducerList = userProducerListResult.getResult();
            }
        }

        // 获取消费者
        List<Consumer> consumerList = null;
        List<Long> consumerIdList = auditBatchAssociate.getConsumerIdList();
        if (consumerIdList != null) {
            Result<List<Consumer>> consumerListResult = consumerService.queryByIdList(consumerIdList);
            if (consumerListResult.isNotEmpty()) {
                consumerList = consumerListResult.getResult();
            }
        }

        // 获取topic
        List<Topic> topicList = getTopicList(userProducerList, consumerList);

        // 拼装vo
        List<TopicInfoVO> topicInfoVOList = new ArrayList<>();
        for (Topic topic : topicList) {
            TopicInfoVO ti = new TopicInfoVO();
            ti.setTopic(topic);
            addUserProducer(ti, userProducerList);
            addConsumer(ti, consumerList);
            topicInfoVOList.add(ti);
        }

        UserTopicInfoVO userTopicInfoVO = new UserTopicInfoVO(userListResult.getResult(), topicInfoVOList);
        userTopicInfoVO.setAid(aid);
        return Result.getResult(userTopicInfoVO);
    }

    private List<Topic> getTopicList(List<UserProducer> producerList, List<Consumer> consumerList) {
        Set<Long> topicIdSet = new HashSet<>();
        if (producerList != null) {
            for (UserProducer up : producerList) {
                topicIdSet.add(up.getTid());
            }
        }
        if (consumerList != null) {
            for (Consumer consumer : consumerList) {
                topicIdSet.add(consumer.getTid());
            }
        }
        if (topicIdSet.size() != 0) {
            Result<List<Topic>> topicListResult = topicService.queryTopicList(topicIdSet);
            if (topicListResult.isNotEmpty()) {
                return topicListResult.getResult();
            }
        }
        return null;
    }

    private void addUserProducer(TopicInfoVO ti, List<UserProducer> list) {
        if (list == null) {
            return;
        }
        for (UserProducer up : list) {
            if (up.getTid() == ti.getTopic().getId()) {
                ti.addUserProducer(up);
            }
        }
    }

    private void addConsumer(TopicInfoVO ti, List<Consumer> list) {
        if (list == null) {
            return;
        }
        for (Consumer consumer : list) {
            if (consumer.getTid() == ti.getTopic().getId()) {
                ti.addConsumer(consumer);
            }
        }
    }

    /**
     * 获取管理员审核
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getBecomAdminResult(long aid) {
        Result<Audit> result = queryAudit(aid);
        if (result.isNotOK()) {
            return result;
        }
        AuditVO auditVo = new AuditVO();
        BeanUtils.copyProperties(result.getResult(), auditVo);

        Result<User> userResult = userService.query(auditVo.getUid());
        if (userResult.isNotOK()) {
            return userResult;
        }
        auditVo.setUser(userResult.getResult());
        return Result.getResult(auditVo);
    }

    /**
     * 获取 userProducer申请删除 信息
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getDeleteUserProducerResult(long aid) {
        Result<Audit> auditResult = queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 查询userProducer删除审核记录
        Result<AuditUserProducerDelete> auditUserProducerDeleteResult = auditUserProducerDeleteService
                .queryAuditUserProducerDelete(aid);
        if (auditUserProducerDeleteResult.isNotOK()) {
            return auditUserProducerDeleteResult;
        }
        AuditUserProducerDelete AuditUserProducerDelete = auditUserProducerDeleteResult.getResult();
        // 当前被审核的用户
        User user = null;
        // 此为新增字段，兼容以前的数据
        if (AuditUserProducerDelete.getUid() != 0) {
            Result<User> userResult = userService.query(AuditUserProducerDelete.getUid());
            if (userResult.isNotOK()) {
                return userResult;
            }
            user = userResult.getResult();
        }
        // 以前的数据默认用户名未知
        if (user == null) {
            user = new User();
            user.setName("未知");
        }
        // 已经同意过的，数据已经删除了
        if (StatusEnum.AGREE.getStatus() == auditResult.getResult().getStatus()) {
            AuditUserProducerDeleteVO auditUserProducerDeleteVO = new AuditUserProducerDeleteVO();
            Topic topic = new Topic();
            topic.setName(auditUserProducerDeleteResult.getResult().getTopic());
            auditUserProducerDeleteVO.setTopic(topic);
            UserProducer userProducer = new UserProducer();
            userProducer.setProducer(auditUserProducerDeleteResult.getResult().getProducer());
            auditUserProducerDeleteVO.setUserProducer(userProducer);
            auditUserProducerDeleteVO.setAid(aid);
            auditUserProducerDeleteVO.setUser(user);
            auditUserProducerDeleteVO.setCommit(true);
            return Result.getResult(auditUserProducerDeleteVO);
        }
        AuditUserProducerDelete auditUserProducerDelete = auditUserProducerDeleteResult.getResult();
        // 查询userProducer信息
        Result<UserProducer> userProducerResult = userProducerService
                .findUserProducer(auditUserProducerDelete.getPid());
        if (userProducerResult.isNotOK()) {
            return userProducerResult;
        }
        // 当前删除的用户-生产者关系
        UserProducer userProducer = userProducerResult.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(userProducer.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        // 兼容老数据
        if ("未知".equals(user.getName())) {
            Result<User> queryResult = userService.query(userProducer.getUid());
            if (queryResult.isOK()) {
                user = queryResult.getResult();
            }
        }
        // 查询当前生产者所有的关联用户
        Result<List<UserProducer>> userProducerListResult = userProducerService
                .queryUserProducer(auditUserProducerDelete.getProducer());
        if (userProducerListResult.isNotOK()) {
            return userProducerListResult;
        }
        // 保存所有与当前生产者关联的用户
        List<Long> userIdList = new ArrayList<Long>();
        for (UserProducer up : userProducerListResult.getResult()) {
            userIdList.add(up.getUid());
        }
        Result<List<User>> userResult = userService.query(userIdList);
        if (userResult.isNotOK()) {
            return userResult;
        }
        // 组装vo
        AuditUserProducerDeleteVO auditUserProducerDeleteVO = new AuditUserProducerDeleteVO();
        auditUserProducerDeleteVO.setTopic(topicResult.getResult());
        auditUserProducerDeleteVO.setUserProducer(userProducer);
        auditUserProducerDeleteVO.setUser(user);
        auditUserProducerDeleteVO.setAid(aid);
        auditUserProducerDeleteVO.setUserList(userResult.getResult());
        auditUserProducerDeleteVO.setCommit(false);
        return Result.getResult(auditUserProducerDeleteVO);
    }

    /**
     * 获取 UserConsumer申请删除 信息
     * 
     * @param aid
     * @return
     */
    private Result<?> getDeleteUserConsumerResult(long aid) {
        Result<Audit> auditResult = queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 查询UserConsumer删除审核记录
        Result<AuditUserConsumerDelete> auditUserConsumerDeleteResult = auditUserConsumerDeleteService
                .queryAuditUserConsumerDelete(aid);
        if (auditUserConsumerDeleteResult.isNotOK()) {
            return auditUserConsumerDeleteResult;
        }
        AuditUserConsumerDelete AuditUserConsumerDelete = auditUserConsumerDeleteResult.getResult();
        Result<Consumer> consumerResult = consumerService.queryConsumerByName(AuditUserConsumerDelete.getConsumer());
        if (consumerResult.isNotOK()) {
            return consumerResult;
        }

        // 当前被审核的用户
        User user = null;
        // 此为新增字段，兼容以前的数据
        if (AuditUserConsumerDelete.getUid() != 0) {
            Result<User> userResult = userService.query(AuditUserConsumerDelete.getUid());
            if (userResult.isNotOK()) {
                return userResult;
            }
            user = userResult.getResult();
        }
        // 以前的数据默认用户名未知
        if (user == null) {
            user = new User();
            user.setName("未知");
        }
        // 已经同意过的，数据已经删除了
        if (StatusEnum.AGREE.getStatus() == auditResult.getResult().getStatus()) {
            AuditUserConsumerDeleteVO auditUserConsumerDeleteVO = new AuditUserConsumerDeleteVO();
            Topic topic = new Topic();
            topic.setName(AuditUserConsumerDelete.getTopic());
            auditUserConsumerDeleteVO.setTopic(topic);
            auditUserConsumerDeleteVO.setConsumer(consumerResult.getResult());
            auditUserConsumerDeleteVO.setAid(aid);
            auditUserConsumerDeleteVO.setUser(user);
            auditUserConsumerDeleteVO.setCommit(true);
            return Result.getResult(auditUserConsumerDeleteVO);
        }

        // 查询userConsumer信息a
        Result<UserConsumer> userConsumerResult = userConsumerService.selectById(AuditUserConsumerDelete.getUcid());
        if (userConsumerResult.isNotOK()) {
            return userConsumerResult;
        }
        UserConsumer userConsumer = userConsumerResult.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(userConsumer.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        // 兼容老数据
        if ("未知".equals(user.getName())) {
            Result<User> queryResult = userService.query(userConsumer.getUid());
            if (queryResult.isOK()) {
                user = queryResult.getResult();
            }
        }
        // 查询当前生产者所有的关联用户
        Result<List<UserConsumer>> userConsumerListResult = userConsumerService
                .queryUserConsumerByConsumerId(userConsumer.getConsumerId());
        if (userConsumerListResult.isNotOK()) {
            return userConsumerListResult;
        }
        // 保存所有与当前生产者关联的用户
        List<User> userList = new ArrayList<User>();
        for (UserConsumer uc : userConsumerListResult.getResult()) {
            Result<User> userResult = userService.query(uc.getUid());
            if (userResult.isNotOK()) {
                return userResult;
            }
            userList.add(userResult.getResult());
        }

        // 组装vo
        AuditUserConsumerDeleteVO auditUserConsumerDeleteVO = new AuditUserConsumerDeleteVO();
        auditUserConsumerDeleteVO.setTopic(topicResult.getResult());
        auditUserConsumerDeleteVO.setConsumer(consumerResult.getResult());
        auditUserConsumerDeleteVO.setUser(user);
        auditUserConsumerDeleteVO.setAid(aid);
        auditUserConsumerDeleteVO.setUserList(userList);
        auditUserConsumerDeleteVO.setCommit(false);
        return Result.getResult(auditUserConsumerDeleteVO);
    }

    @Transactional
    public Result<?> saveBatchAssociate(List<UserProducer> upList, List<UserConsumer> ucList) {
        try {
            // 过滤已经存在的UserProducer
            Iterator<UserProducer> upIterator = upList.iterator();
            while (upIterator.hasNext()) {
                UserProducer up = upIterator.next();
                List<Long> tidList = userProducerDao.selectTidByProducerAndUid(up.getUid(), up.getProducer());
                if (tidList == null) {
                    continue;
                }
                for (Long tid : tidList) {
                    if (up.getTid() == tid) {
                        upIterator.remove();
                        break;
                    }
                }
            }
            // 批量保存
            if (upList.size() > 0) {
                userProducerDao.batchInsert(upList);
            }

            // 过滤已经存在的UserConsumer
            Iterator<UserConsumer> ucIterator = ucList.iterator();
            while (ucIterator.hasNext()) {
                UserConsumer uc = ucIterator.next();
                List<Long> tidList = userConsumerDao.selectTidByUidAndConsumerId(uc.getUid(), uc.getConsumerId());
                if (tidList == null) {
                    continue;
                }
                for (Long tid : tidList) {
                    if (uc.getTid() == tid) {
                        ucIterator.remove();
                        break;
                    }
                }
            }
            // 批量保存
            if (ucList.size() > 0) {
                userConsumerDao.batchInsert(ucList);
            }
        } catch (Exception e) {
            logger.error("insert err, upList:{}, ucList:{}", upList, ucList, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 获取消费者配置
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getConsumerConfig(long aid) {
        Result<AuditConsumerConfig> result = auditConsumerConfigService.query(aid);
        if (result.isNotOK()) {
            return result;
        }
        AuditConsumerConfig auditConsumerConfig = result.getResult();
        Result<Consumer> consumerResult = consumerService.queryById(auditConsumerConfig.getConsumerId());
        if (consumerResult.isNotOK()) {
            return consumerResult;
        }
        Result<Topic> topicResult = topicService.queryTopic(consumerResult.getResult().getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        AuditConsumerConfigVO auditConsumerConfigVO = new AuditConsumerConfigVO();
        BeanUtils.copyProperties(auditConsumerConfig, auditConsumerConfigVO);
        auditConsumerConfigVO.setTopic(topicResult.getResult().getName());
        auditConsumerConfigVO.setConsumer(consumerResult.getResult().getName());
        return Result.getResult(auditConsumerConfigVO);
    }

    /**
     * 获取topic流量预警信息
     *
     * @param aid
     * @return Result
     */
    private Result<?> getUpdateTopicTrafficWarnResult(long aid) {
        Result<AuditTopicTrafficWarn> result = auditTopicTrafficWarnService.queryAuditTopicTrafficWarn(aid);
        if (result.isNotOK()) {
            return result;
        }
        AuditTopicTrafficWarn auditTopicTrafficWarn = result.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(auditTopicTrafficWarn.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        // 组装vo
        AuditTopicTrafficWarnVO auditTopicTrafficWarnVO = new AuditTopicTrafficWarnVO();
        BeanUtils.copyProperties(auditTopicTrafficWarn, auditTopicTrafficWarnVO);
        auditTopicTrafficWarnVO.setTopic(topicResult.getResult());
        return Result.getResult(auditTopicTrafficWarnVO);
    }

    /**
     * 保存审核以及消费者配置
     * 
     * @return
     */
    @Transactional
    public Result<?> saveAuditAndConsumerConfig(Audit audit, AuditConsumerConfig auditConsumerConfig) {
        Long count = null;
        try {
            count = auditDao.insert(audit);
            if (count != null && count > 0) {
                auditConsumerConfig.setAid(audit.getId());
                auditConsumerConfigDao.insert(auditConsumerConfig);
            }
        } catch (Exception e) {
            logger.error("insert err, audit:{}", audit, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(audit);
    }
}
