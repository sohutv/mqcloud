package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.bo.Audit.StatusEnum;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.TopicParam;
import com.sohu.tv.mq.cloud.web.controller.param.UserConsumerParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 校验数据是否重复 （主要是：过滤用户重复的申请）
 * 
 * @author zhehongyuan
 * @date 2018年9月14日
 */
@Service
public class VerifyDataService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditService auditService;

    @Autowired
    private AssociateProducerService associateProducerService;

    @Autowired
    private AssociateConsumerService associateConsumerService;

    @Autowired
    private UserProducerService userProducerService;

    @Autowired
    private UserConsumerService userConsumerService;

    @Autowired
    private AuditUserProducerDeleteService auditUserProducerDeleteService;

    @Autowired
    private AuditUserConsumerDeleteService auditUserConsumerDeleteService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private AuditTopicDeleteService auditTopicDeleteService;

    @Autowired
    private AuditConsumerDeleteService auditConsumerDeleteService;

    @Autowired
    private AuditTopicUpdateService auditTopicUpdateService;

    /**
     * 判定关联关系存在依据 user_producer表中存在该关联关系即为存在
     * user_producer表中不存在该关联关系时，当且仅当audit_associate_producer表中的记录在audit表中的审核状态为未审核时，即为存在（避免重复发送审核信息）
     * 默认此功能上线时没有待审核记录 校验用户生产者关联
     * 
     * @param uid
     * @param producer
     * @return
     */
    public Result<?> verifyUserProducerIsExist(TypeEnum type, long uid, long tid, String producer) {
        // 增加校验，用户不可重复关联同一producer
        Result<List<UserProducer>> userProducerResult = userProducerService.findUserProducer(uid, producer);
        if (userProducerResult.isNotOK()) {
            return userProducerResult;
        }
        if (userProducerResult.getResult().size() > 0) {
            return Result.getResult(Status.REPEAT_ERROR);
        }
        // 增加校验用户不可重复发送审核信息
        Result<?> findUserConsumerResult = findAuditRecordsForNotReview(type, uid, producer);
        if (findUserConsumerResult.isNotOK()) {
            return findUserConsumerResult;
        }
        // 校验生产者的名称不可重复，主要是判断其他topic有无此生产者
        Result<List<UserProducer>> queryUserProducerResult = userProducerService.queryUserProducer(producer);
        if (queryUserProducerResult.isNotOK()) {
            return Result.getResult(Status.DB_ERROR);
        }
        for (UserProducer up : queryUserProducerResult.getResult()) {
            if (up.getTid() != tid) {
                return Result.getResult(Status.PRODUCER_REPEAT);
            }
            // 新建生产者时，不可重复
            if (TypeEnum.NEW_PRODUCER == type && producer.equals(up.getProducer())) {
                return Result.getResult(Status.PRODUCER_REPEAT);
            }
        }
        // 正在审核的数据
        Result<List<AuditAssociateProducer>> associateProducerListResult = associateProducerService
                .queryByProducerAndTid(tid, producer);
        if (associateProducerListResult.isNotOK()) {
            return Result.getResult(Status.DB_ERROR);
        }
        if (associateProducerListResult.getResult().size() > 0) {
            return Result.getResult(Status.PRODUCER_REPEAT);
        }
        logger.info("verify add userProducer is ok, uid:{}, producer:{}", uid, producer);
        return Result.getOKResult();
    }

    /**
     * 校验用户消费者关联
     * 
     * @param uid
     * @param cid
     * @return
     */
    public Result<?> verifyUserConsumerIsExist(long uid, long cid) {
        // 增加校验，用户不可重复关联同一consumer
        Result<List<UserConsumer>> userConsumerResult = userConsumerService.queryUserConsumer(uid, cid);
        if (userConsumerResult.isNotOK()) {
            return userConsumerResult;
        }
        if (userConsumerResult.getResult().size() > 0) {
            return Result.getResult(Status.REPEAT_ERROR);
        }
        // 增加校验用户不可重复发送审核信息
        Result<?> findResult = findAuditRecordsForNotReview(TypeEnum.ASSOCIATE_CONSUMER, uid, String.valueOf(cid));
        logger.info("verify add userConsumer is ok, uid:{}, cid:{}", uid, cid);
        return findResult;
    }

    /**
     * 用户消费者关系删除审核,校验那些重复的申请
     * 
     * @param uid
     * @param cid
     * @return
     */
    public Result<?> verifyDeleteRecordUserConsumerIsExist(long uid, long cid) {
        // 增加校验，判断要删除的关系是否存在，主要防止浏览器缓存传过来失效的参数
        Result<List<UserConsumer>> userConsumerResult = userConsumerService.queryUserConsumer(uid, cid);
        if (userConsumerResult.isNotOK()) {
            return Result.getResult(Status.DB_ERROR);
        }
        if (userConsumerResult.getResult().size() == 0) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        // 增加校验用户不可重复发送审核信息, 过滤审核记录的审核结果，同意，拒绝，未处置
        Result<?> findUserConsumerResult = findAuditRecordsForNotReview(TypeEnum.DELETE_USERCONSUMER,
                userConsumerResult.getResult().get(0).getId(), null);
        logger.info("verify delete userConsumer is ok, uid:{}, cid:{}", uid, cid);
        return findUserConsumerResult;
    }

    /**
     * 用户生产者关系删除审核，校验那些重复的申请
     * 
     * @param pid
     * @return
     */
    public Result<?> verifyDeleteRecordUserProducerIsExist(long pid) {
        // 增加校验，判断要删除的关系是否存在，主要防止浏览器缓存传过来失效的参数
        Result<UserProducer> currentUserProducer = userProducerService.findUserProducer(pid);
        if (currentUserProducer.isNotOK()) {
            if (currentUserProducer.getStatus() == Status.NO_RESULT.getKey()) {
                return Result.getResult(Status.PARAM_ERROR);
            }
            return currentUserProducer;
        }
        // 增加校验用户不可重复发送审核信息 ,过滤审核记录的审核结果，同意，拒绝，未处置
        Result<?> findUserProducerResult = findAuditRecordsForNotReview(TypeEnum.DELETE_USERPRODUCER, pid, null);
        logger.info("verify delete userProducer is ok, uid:{}, producer:{}", currentUserProducer.getResult().getUid(),
                currentUserProducer.getResult().getProducer());
        return findUserProducerResult;
    }

    /**
     * 添加topic时的校验，保证topic名称全局唯一
     * 
     * @param topicParam
     * @return
     */
    public Result<?> verifyAddTopicIsExist(TopicParam topicParam) {
        // 排除topic冲突
        Result<Topic> topicResult = topicService.queryTopic(topicParam.getName());
        if (topicResult.getResult() != null) {
            return Result.getResult(Status.TOPIC_REPEAT);
        }
        // 排除topic名称与consumer名称冲突的可能性
        Result<Consumer> consumerResult = consumerService.queryConsumerByName(topicParam.getName());
        if (consumerResult.getResult() != null) {
            return Result.getResult(Status.CONSUMER_REPEAT);
        }
        Result<?> isProducerRepeat = verifyProducerName(topicParam.getProducer());
        if (isProducerRepeat.isNotOK()) {
            return isProducerRepeat;
        }
        return Result.getOKResult();
    }

    /**
     * 校验添加消费者重复申请以及判断消费者名称是否与topic名称相同
     * 
     * @param consumerParam
     * @return
     */
    public Result<?> verifyAddConsumerIsExist(long uid, String consumer) {
        // 排除topic名称与consumer名称冲突的可能性
        Result<Topic> topicResult = topicService.queryTopic(consumer);
        if (topicResult.getResult() != null) {
            return Result.getResult(Status.TOPIC_REPEAT);
        }
        Result<?> isProducerRepeat = verifyProducerName(consumer);
        if (isProducerRepeat.isNotOK()) {
            return isProducerRepeat;
        }
        // 校验是否与存在的消费者重复
        Result<Consumer> consumerResult = consumerService.queryConsumerByName(consumer);
        if (consumerResult.getResult() != null) {
            return Result.getResult(Status.CONSUMER_REPEAT);
        }
        logger.info("verify add consumer is ok, consumer:{}, uid:{}", consumer, uid);
        return Result.getOKResult();
    }

    /**
     * topic更新，队列数量不变，视为不变，主要用于校验用户是否重复发送审核
     * 
     * @param tid queueNum
     * @return
     */
    public Result<?> verifyUpdateTopicIsExist(long tid, int queueNum) {
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        // 队列数量未改变视为本次更新请求无效
        if (queueNum == topicResult.getResult().getQueueNum()) {
            return Result.getResult(Status.DATA_NO_CHANGE);
        }
        // 校验删除申请是否重复
        Result<?> findTopicResult = findAuditRecordsForNotReview(TypeEnum.UPDATE_TOPIC, tid, String.valueOf(queueNum));
        if (findTopicResult.isNotOK()) {
            return findTopicResult;
        }
        logger.info("verify update topic is ok, tid:{}, queueNum:{}", tid, queueNum);
        return findTopicResult;
    }

    /**
     * topic删除 主要用于校验用户是否重复发送审核
     * 
     * @param tid
     * @return
     */
    public Result<?> verifyDeleteTopicIsExist(long tid) {
        // 校验删除申请是否重复
        Result<?> findTopicResult = findAuditRecordsForNotReview(TypeEnum.DELETE_TOPIC, tid, "");
        if (findTopicResult.isNotOK()) {
            return findTopicResult;
        }
        // 校验该topic下是否有消费者
        Result<List<Consumer>> consumerListResult = consumerService.queryByTid(tid);
        if (consumerListResult.isNotOK()) {
            return consumerListResult;
        }
        if (consumerListResult.getResult().size() > 0) {
            return Result.getResult(Status.DELETE_ERR_CONSUMER_EXIST_RESULT);
        }
        // 判断是否存在待审核的
        Result<?> findConsumerResult = findAuditRecordsForNotReview(TypeEnum.ASSOCIATE_CONSUMER, tid, "tid");
        if (findConsumerResult.isNotOK()) {
            return findConsumerResult;
        }
        logger.info("verify delete topic is ok, tid:{}", tid);
        return Result.getOKResult();
    }

    /**
     * 校验消费者删除，主要用于校验用户是否重复发送审核
     * 
     * @param UserConsumerParam
     * @return
     */
    public Result<?> verifyDeleteConsumerIsExist(UserConsumerParam userConsumerParam) {
        // 校验删除申请是否重复
        Result<?> findDeleteResult = findAuditRecordsForNotReview(TypeEnum.DELETE_CONSUMER,
                userConsumerParam.getConsumerId(), "");
        logger.info("verify delete consumer is ok, cid:{}, tid:{}", userConsumerParam.getConsumerId(),
                userConsumerParam.getTid());
        return findDeleteResult;
    }

    /**
     * 用于验证名称是否跟生产者名称重复（包括正在审核中的数据）
     * 
     * @param producer
     * @return
     */
    private Result<?> verifyProducerName(String producer) {
        // 验证是否与生产者名称冲突
        Result<List<UserProducer>> queryUserProducerResult = userProducerService.queryUserProducer(producer);
        if (queryUserProducerResult.isNotOK()) {
            return queryUserProducerResult;
        }
        if (queryUserProducerResult.getResult().size() > 0) {
            return Result.getResult(Status.PRODUCER_REPEAT);
        }
        // 验证是否与正在审核中的生产者名称冲突，不包括同意的和驳回的申请
        Result<?> findResult = findAuditRecordsForNotReview(TypeEnum.ASSOCIATE_PRODUCER, 0L, producer);
        return findResult;
    }

    /**
     * 用于校验是否存在待审核的记录
     * 
     * @param typeEnum
     * @param id
     * @param name
     * @return
     */
    private Result<?> findAuditRecordsForNotReview(TypeEnum typeEnum, long id, String name) {
        Audit audit = new Audit();
        audit.setType(typeEnum.getType());
        audit.setStatus(StatusEnum.INIT.getStatus());
        Result<List<Audit>> auditListResult = auditService.queryAuditList(audit);
        if (auditListResult.getStatus() != Status.NO_RESULT.getKey()) {
            if (auditListResult.isNotOK()) {
                return Result.getResult(Status.DB_ERROR);
            }
            switch (typeEnum) {
                case UPDATE_TOPIC:
                    return findUpdateTopicRecords(auditListResult, id, Integer.parseInt(name));
                case DELETE_TOPIC:
                    return findDeleteTopicRecords(auditListResult, id);
                case DELETE_CONSUMER:
                    return findDeleteConsumerRecords(auditListResult, id);
                case ASSOCIATE_PRODUCER:
                case NEW_PRODUCER:
                    return findAssociateProducerRecords(auditListResult, name, id);// id==0?name:id
                case ASSOCIATE_CONSUMER:
                    return findAssociateConsumerRecords(auditListResult, id, name);
                case DELETE_USERPRODUCER:
                    return findUserProducerDeleteRecords(auditListResult, id);
                case DELETE_USERCONSUMER:
                    return findUserConsumerDeleteRecords(auditListResult, id);
                default:
                    break;
            }

        }
        return Result.getOKResult();
    }

    private Result<?> findAssociateProducerRecords(Result<List<Audit>> result, String name, long uid) {
        for (Audit audit : result.getResult()) {
            Result<AuditAssociateProducer> auditAssociateProducerResult = associateProducerService
                    .query(audit.getId());
            if (auditAssociateProducerResult.isNotOK()) {
                return auditAssociateProducerResult;
            }
            if (uid == 0) {
                if (name.equals(auditAssociateProducerResult.getResult().getProducer())) {
                    return Result.getResult(Status.PRODUCER_REPEAT);
                }
            } else {
                if (uid == auditAssociateProducerResult.getResult().getUid()
                        && auditAssociateProducerResult.getResult().getProducer().equals(name)) {
                    return Result.getResult(Status.AUDIT_RECORD_REPEAT);
                }
            }

        }
        return Result.getOKResult();
    }

    private Result<?> findAssociateConsumerRecords(Result<List<Audit>> result, long id, String name) {
        for (Audit audit : result.getResult()) {
            Result<AuditAssociateConsumer> auditAssociateConsumerResult = associateConsumerService.query(audit.getId());
            if (auditAssociateConsumerResult.isNotOK()) {
                return auditAssociateConsumerResult;
            }
            if ("tid".equals(name)) {
                if (id == auditAssociateConsumerResult.getResult().getTid()) {
                    // 仅提示管理员此topic的删除会有问题
                    return Result.getResult(Status.DELETE_ERR_USER_CONSUMER_EXIST_RESULT);
                }
            } else {
                if (id == auditAssociateConsumerResult.getResult().getUid()
                        && Integer.parseInt(name) == auditAssociateConsumerResult.getResult().getCid()) {
                    return Result.getResult(Status.AUDIT_RECORD_REPEAT);
                }
            }

        }
        return Result.getOKResult();
    }

    private Result<?> findDeleteConsumerRecords(Result<List<Audit>> result, long cid) {
        for (Audit audit : result.getResult()) {
            Result<AuditConsumerDelete> auditConsumerDeleteResult = auditConsumerDeleteService
                    .queryAuditConsumerDelete(audit.getId());
            if (auditConsumerDeleteResult.isNotOK()) {
                return auditConsumerDeleteResult;
            }
            if (cid == auditConsumerDeleteResult.getResult().getCid()) {
                return Result.getResult(Status.AUDIT_RECORD_REPEAT);
            }
        }
        return Result.getOKResult();
    }

    private Result<?> findDeleteTopicRecords(Result<List<Audit>> result, long tid) {
        for (Audit audit : result.getResult()) {
            Result<AuditTopicDelete> auditTopicDeleteResult = auditTopicDeleteService
                    .queryAuditTopicDelete(audit.getId());
            if (auditTopicDeleteResult.isNotOK()) {
                return auditTopicDeleteResult;
            }
            if (tid == auditTopicDeleteResult.getResult().getTid()) {
                return Result.getResult(Status.AUDIT_RECORD_REPEAT);
            }
        }
        return Result.getOKResult();
    }

    private Result<?> findUpdateTopicRecords(Result<List<Audit>> result, long tid, int queueNum) {
        for (Audit audit : result.getResult()) {
            Result<AuditTopicUpdate> auditTopicUpdateResult = auditTopicUpdateService
                    .queryAuditTopicUpdate(audit.getId());
            if (auditTopicUpdateResult.isNotOK()) {
                return auditTopicUpdateResult;
            }
            // 发送更新，每次队列数量不变视为重复
            if (tid == auditTopicUpdateResult.getResult().getTid()
                    && queueNum == auditTopicUpdateResult.getResult().getQueueNum()) {
                return Result.getResult(Status.AUDIT_RECORD_REPEAT);
            }
        }
        return Result.getOKResult();
    }

    private Result<?> findUserProducerDeleteRecords(Result<List<Audit>> result, long pid) {
        for (Audit audit : result.getResult()) {
            Result<AuditUserProducerDelete> auditUserProducerDeleteResult = auditUserProducerDeleteService
                    .queryAuditUserProducerDelete(audit.getId());
            if (auditUserProducerDeleteResult.isNotOK()) {
                return Result.getResult(Status.DB_ERROR);
            }
            if (auditUserProducerDeleteResult.getResult().getPid() == pid) {
                return Result.getResult(Status.AUDIT_RECORD_REPEAT);
            }
        }
        return Result.getOKResult();
    }

    private Result<?> findUserConsumerDeleteRecords(Result<List<Audit>> result, long ucid) {
        for (Audit audit : result.getResult()) {
            Result<AuditUserConsumerDelete> auditUserConsumerDeleteResult = auditUserConsumerDeleteService
                    .queryAuditUserConsumerDelete(audit.getId());
            if (auditUserConsumerDeleteResult.isNotOK()) {
                return Result.getResult(Status.DB_ERROR);
            }
            if (auditUserConsumerDeleteResult.getResult().getUcid() == ucid) {
                return Result.getResult(Status.AUDIT_RECORD_REPEAT);
            }
        }
        return Result.getOKResult();
    }
}