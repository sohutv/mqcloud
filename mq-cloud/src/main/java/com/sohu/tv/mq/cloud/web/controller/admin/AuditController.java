package com.sohu.tv.mq.cloud.web.controller.admin;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.rocketmq.common.protocol.body.Connection;
import org.apache.rocketmq.common.protocol.body.ProducerConnection;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Audit.StatusEnum;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.AuditAssociateConsumer;
import com.sohu.tv.mq.cloud.bo.AuditAssociateProducer;
import com.sohu.tv.mq.cloud.bo.AuditConsumer;
import com.sohu.tv.mq.cloud.bo.AuditConsumerConfig;
import com.sohu.tv.mq.cloud.bo.AuditConsumerDelete;
import com.sohu.tv.mq.cloud.bo.AuditResendMessage;
import com.sohu.tv.mq.cloud.bo.AuditResetOffset;
import com.sohu.tv.mq.cloud.bo.AuditTopic;
import com.sohu.tv.mq.cloud.bo.AuditTopicDelete;
import com.sohu.tv.mq.cloud.bo.AuditTopicTrace;
import com.sohu.tv.mq.cloud.bo.AuditTopicTrafficWarn;
import com.sohu.tv.mq.cloud.bo.AuditTopicUpdate;
import com.sohu.tv.mq.cloud.bo.AuditUserConsumerDelete;
import com.sohu.tv.mq.cloud.bo.AuditUserProducerDelete;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.ConsumerConfig;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserConsumer;
import com.sohu.tv.mq.cloud.bo.UserMessage;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.AssociateConsumerService;
import com.sohu.tv.mq.cloud.service.AssociateProducerService;
import com.sohu.tv.mq.cloud.service.AuditConsumerConfigService;
import com.sohu.tv.mq.cloud.service.AuditConsumerDeleteService;
import com.sohu.tv.mq.cloud.service.AuditConsumerService;
import com.sohu.tv.mq.cloud.service.AuditResendMessageService;
import com.sohu.tv.mq.cloud.service.AuditResetOffsetService;
import com.sohu.tv.mq.cloud.service.AuditService;
import com.sohu.tv.mq.cloud.service.AuditTopicDeleteService;
import com.sohu.tv.mq.cloud.service.AuditTopicService;
import com.sohu.tv.mq.cloud.service.AuditTopicTraceService;
import com.sohu.tv.mq.cloud.service.AuditTopicUpdateService;
import com.sohu.tv.mq.cloud.service.AuditUserConsumerDeleteService;
import com.sohu.tv.mq.cloud.service.AuditUserProducerDeleteService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.ConsumerConfigService;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.service.UserConsumerService;
import com.sohu.tv.mq.cloud.service.UserMessageService;
import com.sohu.tv.mq.cloud.service.UserProducerService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.service.AuditTopicTrafficWarnService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.AuditVO;
import com.sohu.tv.mq.cloud.web.vo.ConnectionVO;
import com.sohu.tv.mq.cloud.web.vo.TopicInfoVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import com.sohu.tv.mq.cloud.web.vo.UserTopicInfoVO;

/**
 * 审核
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月5日
 */
@Controller
@RequestMapping("/admin/audit")
public class AuditController extends AdminViewController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditTopicService auditTopicService;

    @Autowired
    private UserService userService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserMessageService userMessageService;

    @Autowired
    private AssociateProducerService associateProducerService;

    @Autowired
    private AssociateConsumerService associateConsumerService;

    @Autowired
    private UserProducerService userProducerService;

    @Autowired
    private UserConsumerService userConsumerService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private AuditConsumerService auditConsumerService;

    @Autowired
    private AuditTopicDeleteService auditTopicDeleteService;

    @Autowired
    private AuditConsumerDeleteService auditConsumerDeleteService;

    @Autowired
    private AuditResetOffsetService auditResetOffsetService;

    @Autowired
    private AuditTopicUpdateService auditTopicUpdateService;

    @Autowired
    private AuditUserProducerDeleteService auditUserProducerDeleteService;

    @Autowired
    private AuditUserConsumerDeleteService auditUserConsumerDeleteService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private AuditResendMessageService auditResendMessageService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ConsumerConfigService consumerConfigService;

    @Autowired
    private AuditTopicTraceService auditTopicTraceService;
    
    @Autowired
    private AuditConsumerConfigService auditConsumerConfigService;

    @Autowired
    private AuditTopicTrafficWarnService auditTopicTrafficWarnService;

    /**
     * 审核主列表
     * 
     * @param type
     * @param status
     * @param map
     * @return
     */
    @RequestMapping("/list")
    public String list(@RequestParam(value = "type", defaultValue = "-1") int type,
            @RequestParam(value = "status", defaultValue = "0") int status, Map<String, Object> map) {
        // 设置返回视图及常量
        setView(map, "list");
        setResult(map, "status", Audit.StatusEnum.values());
        setResult(map, "type", Audit.TypeEnum.values());

        // 构造查询参数
        Audit audit = new Audit();
        audit.setType(type);
        audit.setStatus(status);

        // 查询审核列表
        Result<List<Audit>> auditListResult = auditService.queryAuditList(audit);
        if (auditListResult.isEmpty()) {
            return view();
        }

        // 拼装VO
        List<Audit> auditList = auditListResult.getResult();
        List<AuditVO> auditVOList = new ArrayList<AuditVO>(auditList.size());
        Set<Long> idSet = new HashSet<Long>(auditList.size());
        for (Audit auditObj : auditList) {
            AuditVO auditVo = new AuditVO();
            BeanUtils.copyProperties(auditObj, auditVo);
            auditVo.setStatusEnum(StatusEnum.getEnumByStatus(auditObj.getStatus()));
            auditVo.setTypeEnum(TypeEnum.getEnumByType(auditObj.getType()));
            auditVOList.add(auditVo);

            idSet.add(auditObj.getUid());
        }

        // 查询用户
        Result<List<User>> userListResult = userService.query(idSet);
        if (userListResult.isNotEmpty()) {
            // vo赋值
            assgin(userListResult.getResult(), auditVOList);
        }
        setResult(map, auditVOList);
        return view();
    }

    /**
     * 申请详情
     * 
     * @param aid
     * @param map
     * @return
     */
    @RequestMapping("/detail")
    public String detail(@RequestParam("type") int type,
            @RequestParam(value = "aid") long aid, Map<String, Object> map) {
        TypeEnum typeEnum = TypeEnum.getEnumByType(type);
        // 创建topic需要集群信息
        if (TypeEnum.NEW_TOPIC == typeEnum || TypeEnum.UPDATE_TOPIC_TRACE == typeEnum) {
            setResult(map, "clusters", clusterService.getAllMQCluster());
        }
        Result<?> result = auditService.detail(typeEnum, aid);
        setResult(map, result);
        return adminViewModule() + "/" + typeEnum.getView();
    }

    /**
     * 拒绝
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/refuse", method = RequestMethod.POST)
    public Result<?> refuse(UserInfo userInfo,
            @RequestParam("aid") long aid,
            @RequestParam(value = "refuseReason") String refuseReason) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 拒绝
        Audit updateAudit = new Audit();
        updateAudit.setId(audit.getId());
        updateAudit.setAuditor(userInfo.getUser().getEmail());
        updateAudit.setStatus(StatusEnum.REJECT.getStatus());
        updateAudit.setRefuseReason(refuseReason);
        Result<Integer> updateResult = auditService.updateAudit(updateAudit);
        if (updateResult.isOK()) {
            // 拼装提示消息
            TypeEnum typeEnum = TypeEnum.getEnumByType(audit.getType());
            String msg = null;
            switch (typeEnum) {
                case NEW_TOPIC:
                    // 获取AuditTopic
                    Result<AuditTopic> auditTopicResult = auditTopicService.queryAuditTopic(aid);
                    if (auditTopicResult.isOK()) {
                        msg = auditTopicResult.getResult().getName();
                    }
                    break;
                case UPDATE_TOPIC:
                    msg = getUpdateTopicTipMessage(aid);
                    break;
                case DELETE_TOPIC:
                    msg = getDeleteTopicTipMessage(aid);
                    break;
                case NEW_CONSUMER:
                    Result<AuditConsumer> auditConsumerResult = auditConsumerService.queryAuditConsumer(aid);
                    if (auditConsumerResult.isOK()) {
                        msg = auditConsumerResult.getResult().getConsumer();
                    }
                    break;
                case DELETE_CONSUMER:
                    msg = getDeleteConsumerTipMessage(aid);
                    break;
                case RESET_OFFSET:
                case RESET_OFFSET_TO_MAX:
                case RESET_RETRY_OFFSET:
                    msg = getAuditResetOffsetTipMessage(aid);
                    break;
                case ASSOCIATE_PRODUCER:
                    msg = getAuditAssociateProducerTipMessage(aid);
                    break;
                case ASSOCIATE_CONSUMER:
                    msg = getAuditAssociateConsumerTipMessage(aid);
                    break;
                case BECOME_ADMIN:
                    break;
                case DELETE_USERPRODUCER:
                    msg = getDeleteUserProducerTipMessage(aid);
                    break;
                case DELETE_USERCONSUMER:
                    msg = getDeleteUserConsumerTipMessage(aid);
                    break;
                case RESEND_MESSAGE:
                    msg = null;
                    break;
                case UPDATE_TOPIC_TRACE:
                    msg = getUpdateTopicTraceMessage(aid);
                    break;
                case BATCH_ASSOCIATE:
                    break;
                case PAUSE_CONSUME:
                case RESUME_CONSUME:
                case LIMIT_CONSUME:
                    msg = getConsumerConfigMessage(aid);
                    break;
                case UPDATE_TOPIC_TRAFFIC_WARN:
                    msg = getUpdateTopicTrafficWarnMessage(aid);
                    break;
            }
            StringBuilder sb = new StringBuilder("您");
            if (audit.getCreateTime() != null) {
                String createTime = DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(audit.getCreateTime());
                sb.append(createTime);
            }
            sb.append("申请的");
            sb.append(typeEnum.getName());
            if (msg != null) {
                sb.append("[");
                sb.append(msg);
                sb.append("]");
            }
            sb.append("被拒绝。原因:");
            sb.append(refuseReason);
            UserMessage userMessage = new UserMessage();
            userMessage.setMessage(sb.toString());
            userMessage.setUid(audit.getUid());
            userMessageService.save(userMessage);
            sendEmailMessage(audit.getUid(), sb.toString());
            return updateResult;
        }
        return Result.getResult(Status.PARAM_ERROR);
    }

    /**
     * 获取提示消息
     * 
     * @param aid
     * @return
     */
    private String getAuditAssociateProducerTipMessage(long aid) {
        Result<AuditAssociateProducer> auditAssociateProducerResult = associateProducerService.query(aid);
        if (auditAssociateProducerResult.isOK()) {
            return auditAssociateProducerResult.getResult().getProducer();
        }
        return null;
    }

    /**
     * 获取提示消息
     * 
     * @param aid
     * @return
     */
    private String getAuditResetOffsetTipMessage(long aid) {
        Result<AuditResetOffset> auditResetOffsetResult = auditResetOffsetService.queryAuditResetOffset(aid);
        if (auditResetOffsetResult.isNotOK()) {
            return null;
        }
        // 查询consumer信息
        Result<Consumer> consumerResult = consumerService.queryById(auditResetOffsetResult.getResult().getConsumerId());
        if (consumerResult.isNotOK()) {
            return null;
        }
        return consumerResult.getResult().getName();
    }

    /**
     * 获取提示消息
     * 
     * @param aid
     * @return
     */
    public String getUpdateTopicTipMessage(long aid) {
        Result<AuditTopicUpdate> auditTopicUpdateResult = auditTopicUpdateService.queryAuditTopicUpdate(aid);
        if (auditTopicUpdateResult.isNotOK()) {
            return null;
        }
        AuditTopicUpdate auditTopicUpdate = auditTopicUpdateResult.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(auditTopicUpdate.getTid());
        if (topicResult.isNotOK()) {
            return null;
        }
        return topicResult.getResult().getName();
    }

    /**
     * 获取提示消息
     * 
     * @param aid
     * @return
     */
    public String getUpdateTopicTraceMessage(long aid) {
        Result<AuditTopicTrace> auditTopicTraceResult = auditTopicTraceService.queryAuditTopicTrace(aid);
        if (auditTopicTraceResult.isNotOK()) {
            return null;
        }
        AuditTopicTrace auditTopicTrace = auditTopicTraceResult.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(auditTopicTrace.getTid());
        if (topicResult.isNotOK()) {
            return null;
        }
        return topicResult.getResult().getName();
    }

    /**
     * 获取提示消息
     * 
     * @param aid
     * @return
     */
    public String getDeleteTopicTipMessage(long aid) {
        // 查询 topic删除审核记录
        Result<AuditTopicDelete> auditTopicDeleteResult = auditTopicDeleteService.queryAuditTopicDelete(aid);
        if (auditTopicDeleteResult.isNotOK()) {
            return null;
        }
        AuditTopicDelete auditTopicDelete = auditTopicDeleteResult.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(auditTopicDelete.getTid());
        if (topicResult.isNotOK()) {
            return null;
        }
        return topicResult.getResult().getName();
    }

    /**
     * 获取提示消息
     * 
     * @param aid
     * @return
     */
    public String getDeleteConsumerTipMessage(long aid) {
        // 查询consumer删除审核记录
        Result<AuditConsumerDelete> auditConsumerDeleteResult = auditConsumerDeleteService
                .queryAuditConsumerDelete(aid);
        if (auditConsumerDeleteResult.isNotOK()) {
            return null;
        }
        AuditConsumerDelete auditConsumerDelete = auditConsumerDeleteResult.getResult();
        // 查询consumer信息
        Result<Consumer> consumerResult = consumerService.queryById(auditConsumerDelete.getCid());
        if (consumerResult.isNotOK()) {
            return null;
        }
        return consumerResult.getResult().getName();
    }

    /**
     * 获取提示消息
     * 
     * @param aid
     * @return
     */
    private String getAuditAssociateConsumerTipMessage(long aid) {
        Result<AuditAssociateConsumer> auditAssociateConsumerResult = associateConsumerService.query(aid);
        if (auditAssociateConsumerResult.isNotOK()) {
            return null;
        }
        Result<Consumer> consumerResult = consumerService.queryById(auditAssociateConsumerResult.getResult().getCid());
        if (consumerResult.isNotOK()) {
            return null;
        }
        return consumerResult.getResult().getName();
    }

    /**
     * 获取提示消息
     * 
     * @param aid
     * @return
     */
    public String getDeleteUserProducerTipMessage(long aid) {
        // 查询consumer删除审核记录
        Result<AuditUserProducerDelete> auditUserProducerDeleteResult = auditUserProducerDeleteService
                .queryAuditUserProducerDelete(aid);
        if (auditUserProducerDeleteResult.isNotOK()) {
            return null;
        }
        AuditUserProducerDelete auditUserProducerDelete = auditUserProducerDeleteResult.getResult();
        // 查询UserProducer信息
        Result<UserProducer> userProducerResult = userProducerService
                .findUserProducer(auditUserProducerDelete.getPid());
        if (userProducerResult.isNotOK()) {
            return null;
        }
        return userProducerResult.getResult().getProducer();
    }

    /**
     * 获取提示消息
     * 
     * @param aid
     * @return
     */
    public String getDeleteUserConsumerTipMessage(long aid) {
        // 查询consumer删除审核记录
        Result<AuditUserConsumerDelete> auditUserConsumerDeleteResult = auditUserConsumerDeleteService
                .queryAuditUserConsumerDelete(aid);
        if (auditUserConsumerDeleteResult.isNotOK()) {
            return null;
        }
        AuditUserConsumerDelete auditUserConsumerDelete = auditUserConsumerDeleteResult.getResult();
        // 查询Consumer信息
        Result<Consumer> consumerResult = consumerService.queryConsumerByName(auditUserConsumerDelete.getConsumer());
        if (consumerResult.isNotOK()) {
            return null;
        }
        return consumerResult.getResult().getName();
    }

    /**
     * 成为管理员
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/become/admin", method = RequestMethod.POST)
    public Result<?> becomeAdmin(UserInfo userInfo,
            @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 设置管理员
        User user = new User();
        user.setId(audit.getUid());
        user.setType(User.ADMIN);
        // 更新用户
        Result<?> result = userService.update(user);
        if (result.isNotOK()) {
            return Result.getWebResult(result);
        }

        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), null);
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_USER_UPDATE_OK);
    }

    /**
     * 创建topic
     * 
     * @param userInfo
     * @param aid
     * @param cid
     * @param traceClusterId为要创建trace topic的集群id，可为空
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/topic/create", method = RequestMethod.POST)
    public Result<?> createTopic(UserInfo userInfo,
            @RequestParam("aid") long aid, @RequestParam("cid") int cid,
            @RequestParam("traceClusterId") Integer traceClusterId) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验是否合法
        Audit audit = auditResult.getResult();
        if (TypeEnum.NEW_TOPIC.getType() != audit.getType()) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 获取AuditTopic
        Result<AuditTopic> auditTopicResult = auditTopicService.queryAuditTopic(aid);
        if (auditTopicResult.isNotOK()) {
            return auditTopicResult;
        }
        // 获取集群
        Cluster mqCluster = clusterService.getMQClusterById(cid);
        if (mqCluster == null || mqCluster.getId() != cid) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        AuditTopic auditTopic = auditTopicResult.getResult();
        // 创建topic
        Result<?> createResult = topicService.createTopic(mqCluster, audit, auditTopic);
        if (createResult.isNotOK()) {
            return createResult;
        }
        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), auditTopic.getName());
        if (updateOK) {
            // 将trace topic的创建置后，不影响主流程，出错后需要手动创建
            if (auditTopic.traceEnabled()) {
                Result<?> traceTopicResult = topicService.createTraceTopic(audit, auditTopic, traceClusterId);
                if (traceTopicResult.isNotOK()) {
                    return Result.getResult(Status.TOPIC_CREATE_OK_BUT_TRACE_TOPIC_CREATE_ERROR);
                }
            }
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_TOPIC_OK);
    }

    /**
     * 创建消费者
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/consumer/create", method = RequestMethod.POST)
    public Result<?> createConsumer(UserInfo userInfo,
            @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 获取AuditConsumer
        Result<AuditConsumer> auditConsumerResult = auditConsumerService.queryAuditConsumer(aid);
        if (auditConsumerResult.isNotOK()) {
            return auditConsumerResult;
        }
        AuditConsumer auditConsumer = auditConsumerResult.getResult();

        // 查询topic
        Result<Topic> topicResult = topicService.queryTopic(auditConsumer.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        // 查询cluster
        Cluster cluster = clusterService.getMQClusterById(topicResult.getResult().getClusterId());
        if (cluster == null) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 构建userConsumer
        UserConsumer userConsumer = new UserConsumer();
        userConsumer.setUid(audit.getUid());
        userConsumer.setTid(auditConsumer.getTid());

        // 构建consumer
        Consumer consumer = new Consumer();
        BeanUtils.copyProperties(auditConsumer, consumer);
        consumer.setInfo(audit.getInfo());
        // 保存数据
        Result<?> saveResult = userConsumerService.saveUserConsumer(cluster, userConsumer, consumer);
        if (saveResult.isNotOK()) {
            return saveResult;
        }

        // 保存限速数据
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setConsumer(auditConsumer.getConsumer());
        consumerConfig.setPermitsPerSecond(Double.valueOf(auditConsumer.getPermitsPerSecond()));
        consumerConfig.setEnableRateLimit(true);
        Result<?> consumerConfigResult = consumerConfigService.save(consumerConfig);
        if (consumerConfigResult.isNotOK()) {
            logger.error("save consumer{} rate limit error", auditConsumer.getConsumer());
        }
        
        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), consumer.getName());
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_CONSUME_OK);
    }

    /**
     * 关联生产者
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/producer/associate", method = RequestMethod.POST)
    public Result<?> associateProducer(UserInfo userInfo,
            @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 获取AuditAssociateProducer
        Result<AuditAssociateProducer> auditAssociateProducerResult = associateProducerService.query(aid);
        if (auditAssociateProducerResult.isNotOK()) {
            return auditAssociateProducerResult;
        }
        AuditAssociateProducer auditAssociateProducer = auditAssociateProducerResult.getResult();

        // 保存关联关系
        UserProducer up = new UserProducer();
        up.setTid(auditAssociateProducer.getTid());
        up.setUid(auditAssociateProducer.getUid());
        up.setProducer(auditAssociateProducer.getProducer());
        Result<UserProducer> userProducerResult = userProducerService.saveNoException(up);
        if (userProducerResult.isNotOK()) {
            return userProducerResult;
        }

        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), auditAssociateProducer.getProducer());
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_ASSOCIATE_PRODUCER_OK);
    }

    /**
     * 校验生产者链接
     * 
     * @param aid
     * @param map
     * @return
     */
    @RequestMapping("/producer/connection")
    public String validateAssociateProducer(UserInfo userInfo,
            @RequestParam("aid") long aid, Map<String, Object> map) {
        Result.setResult(map, (Object) null);
        String view = adminViewModule() + "/producerConnection";
        // 获取AuditAssociateProducer
        Result<AuditAssociateProducer> auditAssociateProducerResult = associateProducerService.query(aid);
        if (auditAssociateProducerResult.isNotOK()) {
            return view;
        }
        AuditAssociateProducer auditAssociateProducer = auditAssociateProducerResult.getResult();
        // 获取topic
        Result<Topic> topicResult = topicService.queryTopic(auditAssociateProducer.getTid());
        if (topicResult.isNotOK()) {
            return view;
        }
        Topic topic = topicResult.getResult();
        Cluster cluster = clusterService.getMQClusterById(topic.getClusterId());
        Result<ProducerConnection> producerConnectionResult = userProducerService.examineProducerConnectionInfo(
                auditAssociateProducer.getProducer(), topic.getName(), cluster);

        // 组装成vo
        List<ConnectionVO> connectionVOList = new ArrayList<ConnectionVO>();
        if (producerConnectionResult.isOK()) {
            ProducerConnection producerConnection = producerConnectionResult.getResult();
            for (Connection conn : producerConnection.getConnectionSet()) {
                ConnectionVO connectionVO = new ConnectionVO();
                connectionVO.setConnection(conn);
                connectionVOList.add(connectionVO);
            }
        }
        setResult(map, connectionVOList);
        return view;
    }

    /**
     * 关联消费者
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/consumer/associate", method = RequestMethod.POST)
    public Result<?> associateConsumer(UserInfo userInfo,
            @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 获取AuditAssociateConsumer
        Result<AuditAssociateConsumer> auditAssociateConsumerResult = associateConsumerService.query(aid);
        if (auditAssociateConsumerResult.isNotOK()) {
            return auditAssociateConsumerResult;
        }
        AuditAssociateConsumer auditAssociateConsumer = auditAssociateConsumerResult.getResult();

        // 保存关联关系
        UserConsumer uc = new UserConsumer();
        uc.setTid(auditAssociateConsumer.getTid());
        uc.setUid(auditAssociateConsumer.getUid());
        uc.setConsumerId(auditAssociateConsumer.getCid());

        Result<?> userConsumerResult = userConsumerService.saveNoException(uc);
        if (userConsumerResult.isNotOK()) {
            return userConsumerResult;
        }

        // 更新申请状态
        Result<Consumer> consumerResult = consumerService.queryById(auditAssociateConsumer.getCid());
        String tip = consumerResult.isOK() ? consumerResult.getResult().getName() : null;
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), tip);
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_ASSOCIATE_CONSUME_OK);
    }

    /**
     * 删除topic
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/topic/delete", method = RequestMethod.POST)
    public Result<?> deleteTopic(UserInfo userInfo,
            @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 查询 topic删除审核记录
        Result<AuditTopicDelete> auditTopicDeleteResult = auditTopicDeleteService.queryAuditTopicDelete(aid);
        if (auditTopicDeleteResult.isNotOK()) {
            return auditTopicDeleteResult;
        }
        AuditTopicDelete auditTopicDelete = auditTopicDeleteResult.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(auditTopicDelete.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }

        // 删除topic
        Result<?> createResult = topicService.deleteTopic(topicResult.getResult());
        if (createResult.isNotOK()) {
            return createResult;
        }

        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), topicResult.getResult().getName());
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_DELETE_TOPIC_OK);
    }

    /**
     * 更新topic
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/topic/update", method = RequestMethod.POST)
    public Result<?> updateTopic(UserInfo userInfo,
            @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 查询 topic更新审核记录
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
        // 构造更新用的topic
        Topic topic = topicResult.getResult();
        topic.setQueueNum(auditTopicUpdate.getQueueNum());
        // 更新topic
        Result<?> updateResult = topicService.updateTopic(topic);
        if (updateResult.isNotOK()) {
            return updateResult;
        }

        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), topicResult.getResult().getName());
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_DELETE_TOPIC_UPDATE_OK);
    }

    /**
     * 删除consumer
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/consumer/delete", method = RequestMethod.POST)
    public Result<?> deleteConsumer(UserInfo userInfo,
            @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 查询consumer删除审核记录
        Result<AuditConsumerDelete> auditConsumerDeleteResult = auditConsumerDeleteService
                .queryAuditConsumerDelete(aid);
        if (auditConsumerDeleteResult.isNotOK()) {
            return auditConsumerDeleteResult;
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

        // 删除消费者
        Cluster cluster = clusterService.getMQClusterById(topicResult.getResult().getClusterId());
        Result<?> deleteResult = consumerService.deleteConsumer(cluster, consumer, audit.getUid());
        if (deleteResult.isNotOK()) {
            return deleteResult;
        }

        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), consumer.getName());
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_DELETE_CONSUMER_OK);
    }

    /**
     * 删除userProducer
     * 
     * @param aid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/userProducer/delete", method = RequestMethod.POST)
    public Result<?> deleteUserProducer(UserInfo userInfo, @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 查询userProducer删除审核记录
        Result<AuditUserProducerDelete> auditUserProducerDeleteResult = auditUserProducerDeleteService
                .queryAuditUserProducerDelete(aid);
        if (auditUserProducerDeleteResult.isNotOK()) {
            return auditUserProducerDeleteResult;
        }
        AuditUserProducerDelete auditUserProducerDelete = auditUserProducerDeleteResult.getResult();
        // 查询UserProducer信息
        Result<UserProducer> userProducerResult = userProducerService
                .findUserProducer(auditUserProducerDelete.getPid());
        if (userProducerResult.isNotOK()) {
            return userProducerResult;
        }
        UserProducer userProducer = userProducerResult.getResult();
        // 查询topic记录
        Result<Topic> topicResult = topicService.queryTopic(userProducer.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }

        // 删除userProducer
        Result<?> deleteResult = userProducerService.deleteUserProducer(userProducer);
        if (deleteResult.isNotOK()) {
            return deleteResult;
        }

        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), userProducer.getProducer());
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_DELETE_USERPRODUCER_OK);
    }

    /**
     * 删除userConsumer
     * 
     * @param aid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/userConsumer/delete", method = RequestMethod.POST)
    public Result<?> deleteUserConsumer(UserInfo userInfo, @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 查询userConsumer删除审核记录
        Result<AuditUserConsumerDelete> auditUserConsumerDeleteResult = auditUserConsumerDeleteService
                .queryAuditUserConsumerDelete(aid);
        if (auditUserConsumerDeleteResult.isNotOK()) {
            return auditUserConsumerDeleteResult;
        }
        AuditUserConsumerDelete auditUserConsumerDelete = auditUserConsumerDeleteResult.getResult();
        // 查询UserConsumer信息
        Result<UserConsumer> userConsumerResult = userConsumerService.selectById(auditUserConsumerDelete.getUcid());
        if (userConsumerResult.isNotOK()) {
            return userConsumerResult;
        }
        // 删除userConsumer
        Result<?> deleteResult = userConsumerService.deleteById(auditUserConsumerDelete.getUcid());
        if (deleteResult.isNotOK()) {
            return deleteResult;
        }
        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), auditUserConsumerDelete.getConsumer());
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_DELETE_USERCONSUMER_OK);
    }

    /**
     * reset offset
     * 
     * @param aid
     * @param map
     * @return
     * @throws ParseException
     */
    @ResponseBody
    @RequestMapping(value = "/reset/offset", method = RequestMethod.POST)
    public Result<?> resetOffset(UserInfo userInfo, @RequestParam("aid") long aid) throws ParseException {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 查询审核记录
        Result<AuditResetOffset> auditResetOffsetResult = auditResetOffsetService.queryAuditResetOffset(aid);
        if (auditResetOffsetResult.isNotOK()) {
            return auditResetOffsetResult;
        }
        AuditResetOffset auditResetOffset = auditResetOffsetResult.getResult();
        // 查询consumer信息
        Result<Consumer> consumerResult = consumerService.queryById(auditResetOffset.getConsumerId());
        if (consumerResult.isNotOK()) {
            return consumerResult;
        }
        Consumer consumer = consumerResult.getResult();

        // 重试消息重置
        if (TypeEnum.RESET_RETRY_OFFSET.getType() == audit.getType()) {
            Date resetTo = DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).parse(auditResetOffset.getOffset());
            ConsumerConfig consumerConfig = new ConsumerConfig();
            consumerConfig.setConsumer(consumer.getName());
            consumerConfig.setRetryMessageResetTo(resetTo.getTime());
            Result<?> result = consumerConfigService.save(consumerConfig);
            if (result.isNotOK()) {
                return Result.getWebResult(result);
            }
        } else {
            // 查询topic记录
            Result<Topic> topicResult = topicService.queryTopic(auditResetOffset.getTid());
            if (topicResult.isNotOK()) {
                return topicResult;
            }
            Topic topic = topicResult.getResult();
            Result<?> resetOffsetResult = consumerService.resetOffset(topic.getClusterId(), topic.getName(),
                    consumer.getName(), auditResetOffset.getOffset());
            if (resetOffsetResult.isNotOK()) {
                return resetOffsetResult;
            }
        }
        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), consumer.getName());
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_DELETE_RESET_OFFSET_OK);
    }

    /**
     * 同意审核并发送消息
     * 
     * @param audit
     * @param auditMail
     * @param tip
     * @return
     */
    private boolean agreeAndTip(Audit audit, String auditMail, String tip) {
        Audit updateAudit = new Audit();
        updateAudit.setId(audit.getId());
        updateAudit.setAuditor(auditMail);
        updateAudit.setStatus(StatusEnum.AGREE.getStatus());
        Result<Integer> updateResult = auditService.updateAudit(updateAudit);
        if (updateResult.isOK()) {
            UserMessage userMessage = new UserMessage();
            TypeEnum typeEnum = TypeEnum.getEnumByType(audit.getType());
            StringBuilder sb = new StringBuilder("您");
            if (audit.getCreateTime() != null) {
                String createTime = DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(audit.getCreateTime());
                sb.append(createTime);
            }
            sb.append("申请的");
            sb.append(typeEnum.getName());
            if (tip != null) {
                sb.append("[");
                sb.append(tip);
                sb.append("]");
            }
            sb.append("审核通过");
            userMessage.setMessage(sb.toString());
            userMessage.setUid(audit.getUid());
            userMessageService.save(userMessage);
            sendEmailMessage(audit.getUid(), sb.toString());
            return true;
        }
        return false;
    }

    /**
     * resendMessage
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/resend/message", method = RequestMethod.POST)
    public Result<?> resendMessage(UserInfo userInfo, @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 查询审核记录
        Result<List<AuditResendMessage>> listResult = auditResendMessageService.query(aid);
        if (listResult.isNotOK()) {
            return listResult;
        }
        // 未全部重发成功不可审核
        List<AuditResendMessage> auditResendMessageList = listResult.getResult();
        for (AuditResendMessage msg : auditResendMessageList) {
            if (AuditResendMessage.StatusEnum.SUCCESS.getStatus() != msg.getStatus()) {
                return Result.getResult(Status.AUDIT_MESSAGE_CANNOT_AUTID_WHEN_NOT_SEND_OK);
            }
        }
        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), null);
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_DELETE_RESET_OFFSET_OK);
    }

    /**
     * vo赋值
     * 
     * @param from
     * @param to
     */
    private void assgin(List<User> from, List<AuditVO> to) {
        for (AuditVO auditVO : to) {
            for (User user : from) {
                if (auditVO.getUid() == user.getId()) {
                    auditVO.setUser(user);
                    continue;
                }
            }
        }
    }

    /**
     * 为用户发送审核结果
     * 
     * @param uid
     * @param content
     */
    private void sendEmailMessage(long uid, String content) {
        Result<User> userResult = userService.query(uid);
        if (userResult.isNotOK()) {
            logger.warn("select user is not ok! uid:{}", uid);
            return;
        }
        sendEmailMessage(userResult.getResult().getEmail(), content);
    }

    /**
     * 为用户发送审核结果
     * 
     * @param email
     * @param content
     */
    private void sendEmailMessage(String email, String content) {
        if (!alertService.sendMail("MQCloud:审核结果", content, email)) {
            logger.warn("send audit result fail!  email:{}, content:{}", email, content);
        }
    }

    /**
     * 更新topic Trace
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/topic/update/trace", method = RequestMethod.POST)
    public Result<?> updateTopicTrace(UserInfo userInfo,
            @RequestParam("aid") long aid,
            @RequestParam("traceClusterId") int traceClusterId) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 查询 topic更新审核记录
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
        // 构造更新用的topic
        Topic topic = topicResult.getResult();
        // 校验是否需要修改
        if (topic.getTraceEnabled() == auditTopicTrace.getTraceEnabled()) {
            return Result.getResult(Status.NO_NEED_MODIFY_ERROR);
        }
        topic.setTraceEnabled(auditTopicTrace.getTraceEnabled());
        // 更新topic
        Result<?> updateResult = topicService.updateTopicTrace(audit, topic, traceClusterId);
        if (updateResult.isNotOK()) {
            return updateResult;
        }

        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), topicResult.getResult().getName());
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_DELETE_TOPIC_UPDATE_OK);
    }

    /**
     * 更新topic流量预警
     *
     * @param aid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/topic/update/trafficWarn", method = RequestMethod.POST)
    public Result<?> updateTopicTrafficWarn(UserInfo userInfo,
                                      @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        // 查询 topic更新审核记录
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
        // 构造更新用的topic
        Topic topic = topicResult.getResult();
        // 校验是否需要修改
        if (topic.getTrafficWarnEnabled() == auditTopicTrafficWarn.getTrafficWarnEnabled()) {
            return Result.getResult(Status.NO_NEED_MODIFY_ERROR);
        }
        topic.setTrafficWarnEnabled(auditTopicTrafficWarn.getTrafficWarnEnabled());
        // 更新topic
        Result<?> updateResult = topicService.updateTopicTrafficWarn(topic);
        if (updateResult.isNotOK()) {
            return updateResult;
        }
        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), topicResult.getResult().getName());
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_DELETE_TOPIC_UPDATE_OK);
    }

    /**
     * 批量关联
     * 
     * @param aid
     * @param map
     * @return
     */
    @SuppressWarnings({"unchecked"})
    @ResponseBody
    @RequestMapping(value = "/batch/associate", method = RequestMethod.POST)
    public Result<?> batchAssociate(UserInfo userInfo,
            @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        Result<UserTopicInfoVO> userTopicInfoVOResult = (Result<UserTopicInfoVO>) auditService
                .getAuditBatchAssociateResult(aid);
        if (userTopicInfoVOResult.isNotOK()) {
            return Result.getWebResult(userTopicInfoVOResult);
        }

        // 拼装对象
        List<UserProducer> upList = new ArrayList<>();
        List<UserConsumer> ucList = new ArrayList<>();
        UserTopicInfoVO userTopicInfoVO = userTopicInfoVOResult.getResult();
        for (User user : userTopicInfoVO.getUserList()) {
            for (TopicInfoVO topicInfoVO : userTopicInfoVO.getTopicInfoVoList()) {
                // 拼装UserProducer
                for (UserProducer up : topicInfoVO.getProducerList()) {
                    UserProducer tmp = new UserProducer();
                    tmp.setUid(user.getId());
                    tmp.setTid(topicInfoVO.getTopic().getId());
                    tmp.setProducer(up.getProducer());
                    upList.add(tmp);
                }
                // 拼装UserConsumer
                for (Consumer consumer : topicInfoVO.getConsumerList()) {
                    UserConsumer uc = new UserConsumer();
                    uc.setUid(user.getId());
                    uc.setTid(topicInfoVO.getTopic().getId());
                    uc.setConsumerId(consumer.getId());
                    ucList.add(uc);
                }
            }
        }

        // 保存关联关系
        Result<?> result = auditService.saveBatchAssociate(upList, ucList);
        if (result.isNotOK()) {
            return result;
        }

        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), null);
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_ASSOCIATE_PRODUCER_OK);
    }
    
    /**
     * 获取提示消息
     * 
     * @param aid
     * @return
     */
    public String getConsumerConfigMessage(long aid) {
        Result<AuditConsumerConfig> result = auditConsumerConfigService.query(aid);
        if (result.isNotOK()) {
            return null;
        }
        Result<Consumer> consumerResult = consumerService.queryById(result.getResult().getConsumerId());
        if (consumerResult.isNotOK()) {
            return null;
        }
        return consumerResult.getResult().getName();
    }

    public String getUpdateTopicTrafficWarnMessage(long aid) {
        Result<AuditTopicTrafficWarn> result = auditTopicTrafficWarnService.queryAuditTopicTrafficWarn(aid);
        if (result.isNotOK()) {
            return null;
        }
        AuditTopicTrafficWarn auditTopicTrafficWarn = result.getResult();
        Result<Topic> topicResult = topicService.queryTopic(auditTopicTrafficWarn.getTid());
        if (topicResult.isNotOK()) {
            return null;
        }
        return topicResult.getResult().getName();
    }
    
    /**
     * 更新客户端配置
     * 
     * @param aid
     * @param map
     * @return
     * @throws ParseException
     */
    @ResponseBody
    @RequestMapping(value = "/update/consumer/config", method = RequestMethod.POST)
    public Result<?> updateConsumerConfig(UserInfo userInfo, @RequestParam("aid") long aid) throws ParseException {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 查询审核记录
        Result<AuditConsumerConfig> result = auditConsumerConfigService.query(aid);
        if (result.isNotOK()) {
            return result;
        }
        AuditConsumerConfig auditConsumerConfig = result.getResult();
        // 查询consumer信息
        Result<Consumer> consumerResult = consumerService.queryById(auditConsumerConfig.getConsumerId());
        if (consumerResult.isNotOK()) {
            return consumerResult;
        }
        Consumer consumer = consumerResult.getResult();

        // 更新配置表
        ConsumerConfig consumerConfig = new ConsumerConfig();
        BeanUtils.copyProperties(auditConsumerConfig, consumerConfig);
        consumerConfig.setConsumer(consumer.getName());
        Result<?> saveResult = consumerConfigService.save(consumerConfig);
        if (saveResult.isNotOK()) {
            return Result.getWebResult(saveResult);
        }
        // 更新申请状态
        boolean updateOK = agreeAndTip(audit, userInfo.getUser().getEmail(), consumer.getName());
        if (updateOK) {
            return Result.getOKResult();
        }
        return Result.getResult(Status.DB_UPDATE_ERR_UPDATE_CONSUMER_CONFIG_OK);
    }

    @Override
    public String viewModule() {
        return "audit";
    }
}
