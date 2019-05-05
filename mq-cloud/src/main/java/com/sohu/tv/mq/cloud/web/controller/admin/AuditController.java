package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
import com.sohu.tv.mq.cloud.bo.AuditConsumerDelete;
import com.sohu.tv.mq.cloud.bo.AuditResendMessage;
import com.sohu.tv.mq.cloud.bo.AuditResetOffset;
import com.sohu.tv.mq.cloud.bo.AuditTopic;
import com.sohu.tv.mq.cloud.bo.AuditTopicDelete;
import com.sohu.tv.mq.cloud.bo.AuditTopicUpdate;
import com.sohu.tv.mq.cloud.bo.AuditUserConsumerDelete;
import com.sohu.tv.mq.cloud.bo.AuditUserProducerDelete;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserConsumer;
import com.sohu.tv.mq.cloud.bo.UserMessage;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.AssociateConsumerService;
import com.sohu.tv.mq.cloud.service.AssociateProducerService;
import com.sohu.tv.mq.cloud.service.AuditConsumerDeleteService;
import com.sohu.tv.mq.cloud.service.AuditConsumerService;
import com.sohu.tv.mq.cloud.service.AuditResendMessageService;
import com.sohu.tv.mq.cloud.service.AuditResetOffsetService;
import com.sohu.tv.mq.cloud.service.AuditService;
import com.sohu.tv.mq.cloud.service.AuditTopicDeleteService;
import com.sohu.tv.mq.cloud.service.AuditTopicService;
import com.sohu.tv.mq.cloud.service.AuditTopicUpdateService;
import com.sohu.tv.mq.cloud.service.AuditUserConsumerDeleteService;
import com.sohu.tv.mq.cloud.service.AuditUserProducerDeleteService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.service.UserConsumerService;
import com.sohu.tv.mq.cloud.service.UserMessageService;
import com.sohu.tv.mq.cloud.service.UserProducerService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.AuditAssociateConsumerVO;
import com.sohu.tv.mq.cloud.web.vo.AuditAssociateProducerVO;
import com.sohu.tv.mq.cloud.web.vo.AuditConsumerDeleteVO;
import com.sohu.tv.mq.cloud.web.vo.AuditConsumerVO;
import com.sohu.tv.mq.cloud.web.vo.AuditResendMessageVO;
import com.sohu.tv.mq.cloud.web.vo.AuditResetOffsetVO;
import com.sohu.tv.mq.cloud.web.vo.AuditTopicDeleteVO;
import com.sohu.tv.mq.cloud.web.vo.AuditTopicUpdateVO;
import com.sohu.tv.mq.cloud.web.vo.AuditUserConsumerDeleteVO;
import com.sohu.tv.mq.cloud.web.vo.AuditUserProducerDeleteVO;
import com.sohu.tv.mq.cloud.web.vo.AuditVO;
import com.sohu.tv.mq.cloud.web.vo.ConnectionVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import com.sohu.tv.mq.util.CommonUtil;

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
        Result<?> result = null;
        String view = null;
        TypeEnum typeEnum = TypeEnum.getEnumByType(type);
        switch (typeEnum) {
            case NEW_TOPIC:
                setResult(map, "clusters", clusterService.getAllMQCluster());
                view = "addTopic";
                result = auditTopicService.queryAuditTopic(aid);
                break;
            case UPDATE_TOPIC:
                view = "updateTopic";
                result = getUpdateTopicResult(aid);
                break;
            case DELETE_TOPIC:
                view = "deleteTopic";
                result = getDeleteTopicResult(aid);
                break;
            case NEW_CONSUMER:
                view = "addConsumer";
                result = getAuditConumerResult(aid);
                break;
            case DELETE_CONSUMER:
                view = "deleteConsumer";
                result = getDeleteConsumerResult(aid);
                break;
            case RESET_OFFSET:
            case RESET_OFFSET_TO_MAX:
                view = "resetOffset";
                result = getResetOffsetResult(aid);
                break;
            case ASSOCIATE_PRODUCER:
                view = "associateProducer";
                result = getAuditAssociateProducerResult(aid);
                break;
            case ASSOCIATE_CONSUMER:
                view = "associateConsumer";
                result = getAuditAssociateConsumerResult(aid);
                break;
            case BECOME_ADMIN:
                view = "becomeAdmin";
                result = getBecomAdminResult(aid);
                break;
            case DELETE_USERPRODUCER:
                view = "deleteUserProdicer";
                result = getDeleteUserProducerResult(aid);
                break;
            case DELETE_USERCONSUMER:
                view = "deleteUserConsumer";
                result = getDeleteUserConsumerResult(aid);
                break;
            case RESEND_MESSAGE:
                view = "resendMessage";
                result = getResendMessageResult(aid);
                break;
        }
        setResult(map, result);
        return adminViewModule() + "/" + view;
    }
    
    /**
     * 获取管理员审核
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getBecomAdminResult(long aid) {
        Result<Audit> result = auditService.queryAudit(aid);
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
            Result<Audit> auditResult = auditService.queryAudit(aid);
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
            Result<Audit> auditResult = auditService.queryAudit(aid);
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
        auditAssociateConsumerVO.setUser(userResult.getResult().getName() == null ? userResult.getResult().getEmailName()
                        : userResult.getResult().getName());
        return Result.getResult(auditAssociateConsumerVO);
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
            }
            StringBuilder sb = new StringBuilder("您");
            if(audit.getCreateTime() != null) {
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
        if(auditResetOffsetResult.isNotOK()) {
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
     * @param aid
     * @return
     */
    public String getDeleteConsumerTipMessage(long aid) {
        // 查询consumer删除审核记录
        Result<AuditConsumerDelete> auditConsumerDeleteResult = auditConsumerDeleteService.queryAuditConsumerDelete(aid);
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
     * @param aid
     * @return
     */
    public String getDeleteUserProducerTipMessage(long aid) {
        // 查询consumer删除审核记录
        Result<AuditUserProducerDelete> auditUserProducerDeleteResult = auditUserProducerDeleteService.queryAuditUserProducerDelete(aid);
        if (auditUserProducerDeleteResult.isNotOK()) {
            return null;
        }
        AuditUserProducerDelete auditUserProducerDelete = auditUserProducerDeleteResult.getResult();
        // 查询UserProducer信息
        Result<UserProducer> userProducerResult = userProducerService.findUserProducer(auditUserProducerDelete.getPid());
        if (userProducerResult.isNotOK()) {
            return null;
        }
        return userProducerResult.getResult().getProducer();
    }
    
    /**
     * 获取提示消息
     * @param aid
     * @return
     */
    public String getDeleteUserConsumerTipMessage(long aid) {
        // 查询consumer删除审核记录
        Result<AuditUserConsumerDelete> auditUserConsumerDeleteResult = auditUserConsumerDeleteService.queryAuditUserConsumerDelete(aid);
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
                Result<?> traceTopicResult = createTraceTopic(audit, auditTopic, traceClusterId);
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
        if(cluster == null) {
            return Result.getResult(Status.PARAM_ERROR);
        }

        // 构建userConsumer
        UserConsumer userConsumer = new UserConsumer();
        userConsumer.setUid(audit.getUid());
        userConsumer.setTid(auditConsumer.getTid());

        // 构建consumer
        Consumer consumer = new Consumer();
        BeanUtils.copyProperties(auditConsumer, consumer);
        // 保存数据
        Result<?> saveResult = userConsumerService.saveUserConsumer(cluster, userConsumer, consumer);
        if (saveResult.isNotOK()) {
            return saveResult;
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
        Result<AuditConsumerDelete> auditConsumerDeleteResult = auditConsumerDeleteService.queryAuditConsumerDelete(aid);
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
     */
    @ResponseBody
    @RequestMapping(value = "/reset/offset", method = RequestMethod.POST)
    public Result<?> resetOffset(UserInfo userInfo, @RequestParam("aid") long aid) {
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
            if(audit.getCreateTime() != null) {
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
     * 获取 topic申请删除 信息
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getDeleteTopicResult(long aid) {
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if(auditResult.isNotOK()) {
            return auditResult;
        }
        // 查询 topic删除审核记录
        Result<AuditTopicDelete> auditTopicDeleteResult = auditTopicDeleteService.queryAuditTopicDelete(aid);
        if (auditTopicDeleteResult.isNotOK()) {
            return auditTopicDeleteResult;
        }
        // 已经同意过的，数据已经删除了
        if(StatusEnum.AGREE.getStatus() == auditResult.getResult().getStatus()) {
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
        Result<List<UserProducer>> userProducerListResult = userProducerService.queryUserProducerByTid(auditTopicDelete.getTid());
        List<UserProducer> userProducerList = null;
        if (userProducerListResult.isNotEmpty()) {
            userProducerList = userProducerListResult.getResult();
            // 查询用户
            List<Long> uidList = new ArrayList<Long>();
            for(UserProducer up : userProducerList) {
                uidList.add(up.getUid());
            }
            Result<List<User>> userListResult = userService.query(uidList);
            if(userListResult.isEmpty()) {
                return userListResult;
            }
            
            // 赋值用户
            for(UserProducer up : userProducerList) {
                for(User u : userListResult.getResult()) {
                    if(up.getUid() == u.getId()) {
                        if(StringUtils.isBlank(u.getName())) {
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
     * 获取 consumer申请删除 信息
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getDeleteConsumerResult(long aid) {
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if(auditResult.isNotOK()) {
            return auditResult;
        }
        // 查询consumer删除审核记录
        Result<AuditConsumerDelete> auditConsumerDeleteResult = auditConsumerDeleteService.queryAuditConsumerDelete(aid);
        if (auditConsumerDeleteResult.isNotOK()) {
            return auditConsumerDeleteResult;
        }
        // 已经同意过的，数据已经删除了
        if(StatusEnum.AGREE.getStatus() == auditResult.getResult().getStatus()) {
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
        Result<List<UserConsumer>> userConsumerListResult = userConsumerService.queryUserConsumerByConsumerId(consumer.getId());
        if (userConsumerListResult.isNotOK()) {
            return userConsumerListResult;
        }
        // 组装vo
        AuditConsumerDeleteVO auditConsumerDeleteVO = new AuditConsumerDeleteVO();
        auditConsumerDeleteVO.setTopic(topicResult.getResult());
        auditConsumerDeleteVO.setConsumer(consumer);
        auditConsumerDeleteVO.setAid(aid);
        //修改部分逻辑，让那些无主消费者可以删除（限管理员）
        if (!userConsumerListResult.isEmpty()) {
            List<UserConsumer> userConsumerList = userConsumerListResult.getResult();
            // 查询用户
            List<Long> uidList = new ArrayList<Long>();
            for(UserConsumer uc : userConsumerList) {
                uidList.add(uc.getUid());
            }
            Result<List<User>> userListResult = userService.query(uidList);
            if(userListResult.isEmpty()) {
                return userListResult;
            }
            auditConsumerDeleteVO.setUser(userListResult.getResult());
        }        
        return Result.getResult(auditConsumerDeleteVO);
    }

    /**
     * 获取 userProducer申请删除 信息
     * 
     * @param aid
     * @return Result
     */
    private Result<?> getDeleteUserProducerResult(long aid) {
        Result<Audit> auditResult = auditService.queryAudit(aid);
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
        //以前的数据默认用户名未知
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
        //兼容老数据
        if("未知".equals(user.getName())) {
            Result<User> queryResult = userService.query(userProducer.getUid());
            if(queryResult.isOK()) {
                user =  queryResult.getResult();
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
        Result<Audit> auditResult = auditService.queryAudit(aid);
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
        //以前的数据默认用户名未知
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
        //兼容老数据
        if("未知".equals(user.getName())) {
            Result<User> queryResult = userService.query(userConsumer.getUid());
            if(queryResult.isOK()) {
                user =  queryResult.getResult();
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
    
    private Result<?> getResendMessageResult(long aid) {
        // 查询audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 查询AuditResendMessage
        Audit audit = auditResult.getResult();
        Result<List<AuditResendMessage>> auditResendMessageListResult = auditResendMessageService.query(audit.getId());
        if(auditResendMessageListResult.isEmpty()) {
            return auditResendMessageListResult;
        }
        
        // 查询topic
        List<AuditResendMessage> auditResendMessageList = auditResendMessageListResult.getResult();
        Result<Topic> topicResult = topicService.queryTopic(auditResendMessageList.get(0).getTid());
        if(topicResult.isNotOK()) {
            return topicResult;
        }
        
        // 拼装vo
        AuditResendMessageVO auditResendMessageVO = new AuditResendMessageVO();
        auditResendMessageVO.setTopic(topicResult.getResult().getName());
        auditResendMessageVO.setMsgList(auditResendMessageList);
        return Result.getResult(auditResendMessageVO);
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
        for(AuditResendMessage msg : auditResendMessageList) {
            if(AuditResendMessage.StatusEnum.SUCCESS.getStatus() != msg.getStatus()) {
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
     * 创建 trace topic
     * 
     * @param audit
     * @param auditTopic
     * @param traceClusterId
     * @return
     */
    public Result<?> createTraceTopic(Audit audit, AuditTopic auditTopic, Integer traceClusterId) {
        if (traceClusterId == null) {
            return Result.getResult(Status.TRACE_CLUSTER_ID_IS_NULL);
        }
        // 获取集群
        Cluster mqCluster = clusterService.getMQClusterById(traceClusterId);
        if (mqCluster == null) {
            return Result.getResult(Status.TRACE_CLUSTER_IS_NULL);
        }
        // 更改topic名称 例如mqcloud-test-topic转化成mqcloud-test-trace-topic
        auditTopic.setName(CommonUtil.buildTraceTopic(auditTopic.getName()));
        auditTopic.setProducer(CommonUtil.buildTraceTopicProducer(auditTopic.getName()));
        auditTopic.setTraceEnabled(0);
        // 创建topic
        Result<?> createResult = topicService.createTopic(mqCluster, audit, auditTopic);
        if (createResult.isNotOK()) {
            logger.error("create trace topic err ! traceTopic:{}", auditTopic.getName());
            return Result.getResult(Status.TRACE_TOPIC_CREATE_ERROR);
        }
        return Result.getOKResult();
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

    @Override
    public String viewModule() {
        return "audit";
    }
}
