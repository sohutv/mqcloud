package com.sohu.tv.mq.cloud.web.controller.admin;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.service.MQProxyService.ConsumerConfigParam;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.web.vo.WheelCancelMessageVo;
import org.apache.rocketmq.common.message.MessageClientIDSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.Audit.StatusEnum;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.ResendMessageVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 消息
 * 
 * @author yongfeigao
 * @date 2018年12月6日
 */
@Controller
@RequestMapping("/admin/message")
public class AdminMessageController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditResendMessageService auditResendMessageService;

    @Autowired
    private AuditWheelMessageCancelService auditWheelMessageCancelService;

    @Autowired
    private CancelUniqIdService cancelUniqIdService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private MQProxyService mqProxyService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    /**
     * resend message
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/resend", method = RequestMethod.POST)
    public Result<?> resend(UserInfo userInfo, @RequestParam("aid") long aid) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (!mqCloudConfigHelper.canAudit(audit)) {
            return getAuditStatusError(audit.getStatus());
        }

        // 查询审核记录
        Result<List<AuditResendMessage>> listResult = auditResendMessageService.query(aid);
        if (listResult.isEmpty()) {
            return listResult;
        }

        // 获取topic
        List<AuditResendMessage> auditResendMessageList = listResult.getResult();
        Result<Topic> topicResult = topicService.queryTopic(auditResendMessageList.get(0).getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        String topic = topicResult.getResult().getName();
        
        // 获取消费者
        Result<Consumer> consumerResult = auditResendMessageService.queryConsumer(aid);
        if(consumerResult.isNotOK()) {
            return consumerResult;
        }
        Consumer consumer = consumerResult.getResult();

        // 获取cluster
        Cluster cluster = clusterService.getMQClusterById(topicResult.getResult().getClusterId());

        // 统计状态
        ResendMessageVO resendMessageVO = new ResendMessageVO();
        resendMessageVO.setTotal(auditResendMessageList.size());
        // 发送消息
        for (AuditResendMessage msg : auditResendMessageList) {
            if(AuditResendMessage.StatusEnum.SUCCESS.getStatus() == msg.getStatus()) {
                continue;
            }
            Result<?> sendResult;
            if (!consumer.isHttpProtocol()) {
                if (consumer.isClustering()) {
                    sendResult = messageService.resend(cluster, topic, msg.getMsgId(), consumer.getName());
                } else {
                    sendResult = messageService.resendDirectly(cluster, topic, msg.getMsgId(), consumer.getName(),
                            consumer.isProxyRemoting());
                }
            } else {
                ConsumerConfigParam consumerConfigParam = new ConsumerConfigParam();
                consumerConfigParam.setConsumer(consumer.getName());
                consumerConfigParam.setRetryMsgId(msg.getMsgId());
                sendResult = mqProxyService.consumerConfig(userInfo, consumerConfigParam);
            }
            int status = AuditResendMessage.StatusEnum.SUCCESS.getStatus();
            if (sendResult.isNotOK()) {
                logger.warn("resendMessage cluster:{} topic:{} consumer:{} msgId:{} err:{}", cluster, topic,
                        consumer.getName(), msg.getMsgId(), sendResult);
                status = AuditResendMessage.StatusEnum.FAILED.getStatus();
                resendMessageVO.incrFailed();
            } else {
                resendMessageVO.incrSuccess();
            }
            Result<Integer> updateResult = auditResendMessageService.update(aid, msg.getMsgId(), status);
            if (updateResult.isNotOK()) {
                resendMessageVO.incrStatusUpdatedFailed();
                logger.warn("resendMessage cluster:{} topic:{} consumer:{} msgId:{} update not ok :{}", cluster, topic,
                        consumer.getName(), msg.getMsgId(), updateResult);
            }
        }
        
        if(resendMessageVO.getSuccess() > 0 || resendMessageVO.getFailed() > 0) {
            // 查询审核记录
            listResult = auditResendMessageService.query(aid);
            if (listResult.isNotEmpty()) {
                resendMessageVO.setMsgList(listResult.getResult());
            }
        }
        
        if(resendMessageVO.sendAllOK()) {
            return Result.getResult(resendMessageVO);
        } else {
            return Result.getResult(Status.AUDIT_MESSAGE_NOT_SEND_OK).setResult(resendMessageVO);
        }
    }

    /**
     * resend message
     * 
     * @param aid
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/resend/one", method = RequestMethod.POST)
    public Result<?> resendOne(UserInfo userInfo, @RequestParam("aid") long aid, @RequestParam("msgId") String msgId) {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (!mqCloudConfigHelper.canAudit(audit)) {
            return getAuditStatusError(audit.getStatus());
        }

        // 查询审核记录
        Result<AuditResendMessage> auditResendMessageResult = auditResendMessageService.queryOne(aid, msgId);
        if (auditResendMessageResult.isNotOK()) {
            return auditResendMessageResult;
        }

        // 获取topic
        AuditResendMessage auditResendMessage = auditResendMessageResult.getResult();
        Result<Topic> topicResult = topicService.queryTopic(auditResendMessage.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        String topic = topicResult.getResult().getName();

        // 获取cluster
        Cluster cluster = clusterService.getMQClusterById(topicResult.getResult().getClusterId());
        
        // 获取消费者
        Result<Consumer> consumerResult = auditResendMessageService.queryConsumer(aid);
        if(consumerResult.isNotOK()) {
            return consumerResult;
        }
        Consumer consumer = consumerResult.getResult();

        // 统计状态
        ResendMessageVO resendMessageVO = new ResendMessageVO();
        resendMessageVO.setTotal(1);
        if(AuditResendMessage.StatusEnum.SUCCESS.getStatus() != auditResendMessage.getStatus()) {
            Result<?> sendResult;
            if (!consumer.isHttpProtocol()) {
                if (consumerResult.getResult().isClustering()) {
                    sendResult = messageService.resend(cluster, topic, msgId, consumer.getName());
                } else {
                    sendResult = messageService.resendDirectly(cluster, topic, msgId, consumer.getName(),
                            consumer.isProxyRemoting());
                }
            } else {
                ConsumerConfigParam consumerConfigParam = new ConsumerConfigParam();
                consumerConfigParam.setConsumer(consumer.getName());
                consumerConfigParam.setRetryMsgId(msgId);
                sendResult = mqProxyService.consumerConfig(userInfo, consumerConfigParam);
            }
            int status = AuditResendMessage.StatusEnum.SUCCESS.getStatus();
            if (sendResult.isNotOK()) {
                logger.warn("resendMessage cluster:{} topic:{} consumer:{} msgId:{} err:{}", cluster, topic,
                        consumer.getName(), msgId, sendResult);
                status = AuditResendMessage.StatusEnum.FAILED.getStatus();
                resendMessageVO.incrFailed();
            } else {
                resendMessageVO.incrSuccess();
            }
            Result<Integer> updateResult = auditResendMessageService.update(aid, msgId, status);
            if (updateResult.isNotOK()) {
                logger.warn("resendMessage cluster:{} topic:{} consumer:{} msgId:{} update not ok :{}", cluster, topic,
                        consumer.getName(), msgId, updateResult);
                resendMessageVO.incrStatusUpdatedFailed();
            }
            
            // 查询审核记录
            auditResendMessageResult = auditResendMessageService.queryOne(aid, msgId);
            if (auditResendMessageResult.isOK()) {
                List<AuditResendMessage> list = new ArrayList<AuditResendMessage>(1);
                list.add(auditResendMessageResult.getResult());
                resendMessageVO.setMsgList(list);
            }
        }
        
        if(resendMessageVO.sendAllOK()) {
            return Result.getResult(resendMessageVO);
        } else {
            return Result.getResult(Status.AUDIT_MESSAGE_NOT_SEND_OK).setResult(resendMessageVO);
        }
    }

    /**
     * cancel message
     *
     * @param aid
     * @param userInfo
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/cancelWheelMsg", method = RequestMethod.POST)
    public Result<?> cancelWheelMsg(UserInfo userInfo, @RequestParam("aid") long aid) throws ParseException {
        // 获取audit
        Result<Audit> auditResult = auditService.queryAudit(aid);
        if (auditResult.isNotOK()) {
            return auditResult;
        }
        // 校验状态是否合法
        Audit audit = auditResult.getResult();
        if (!mqCloudConfigHelper.canAudit(audit)) {
            return getAuditStatusError(audit.getStatus());
        }
        // 查询审核记录
        Result<List<AuditWheelMessageCancel>> result = auditWheelMessageCancelService.queryNotCancelAuditByAid(aid);
        if (result.isNotOK()) {
            return result;
        }
        List<AuditWheelMessageCancel> auditWheelMessageCancels = result.getResult();
        if (auditWheelMessageCancels.isEmpty()) {
            return Result.getOKResult();
        }
        WheelCancelMessageVo wheelCancelMessageVo = new WheelCancelMessageVo();
        // 检测是否过期
        long now = System.currentTimeMillis() + AuditWheelMessageCancel.MINOR_EXPIRE_CANCEL_TIME;
        List<AuditWheelMessageCancel> unValidAudits = auditWheelMessageCancels.stream()
                .filter(auditWheelMessageCancel -> auditWheelMessageCancel.getDeliverTime() < now)
                .collect(Collectors.toList());
        // 如果全部过期则直接结束，但不可改状态，需人工确认
        if (unValidAudits.size() == auditWheelMessageCancels.size()) {
            String unValidAuditStr = unValidAudits.stream().map(AuditWheelMessageCancel::getUniqueId)
                    .collect(Collectors.joining(","));
            logger.warn("cancel delay message auditId: {}, all message expired", aid);
            return Result.getResult(Status.EXPIRED_UNIQID).formatMessage(unValidAuditStr);
        }
        // 记录执行状态
        auditWheelMessageCancels.removeAll(unValidAudits);
        wheelCancelMessageVo.incrExpiredCancelMsgNum(unValidAudits.size());
        unValidAudits.forEach(auditWheelMessageCancel ->
                wheelCancelMessageVo.addDetail(auditWheelMessageCancel.getUniqueId(), "超时取消"));
        // 解析消息，准备发送取消消息
        AuditWheelMessageCancel auditWheelMessageCancel = auditWheelMessageCancels.get(0);
        Result<Topic> topicResult = topicService.queryTopic(auditWheelMessageCancel.getTid());
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        Topic topic = topicResult.getResult();
        Cluster cluster = clusterService.getMQClusterById(topic.getClusterId());
        if (cluster == null) {
            return Result.getResult(Status.NO_RESULT);
        }
        for (AuditWheelMessageCancel wheelMessageCancel : auditWheelMessageCancels) {
            // 再次校验取消消息是否重复,防止消息发送成功，数据库更新失败场景
            long beginTime = MessageClientIDSetter.getNearlyTimeFromID(wheelMessageCancel.getUniqueId()).getTime() - 5 * 60 * 1000L;
            Result<Boolean> ckeckResult = messageService.checkCancelMessageByKey(topic.getName(), wheelMessageCancel.getUniqueId(),
                    beginTime, cluster);
            if (ckeckResult.isNotOK()) {
                wheelCancelMessageVo.incrFailedCancelMsgNum();
                wheelCancelMessageVo.addDetail(wheelMessageCancel.getUniqueId(), "校验取消消息失败");
                logger.warn("check cancel wheel msg failed, auditWheelMessageCancel:{}", wheelMessageCancel);
                continue;
            }
            // 首次发送取消消息
            if (!ckeckResult.getResult()) {
                // 真实发送前再次校验时间
                long nowTime = System.currentTimeMillis() + AuditWheelMessageCancel.MINOR_EXPIRE_CANCEL_TIME;
                if (wheelMessageCancel.getDeliverTime() < nowTime) {
                    wheelCancelMessageVo.incrExpiredCancelMsgNum();
                    wheelCancelMessageVo.addDetail(wheelMessageCancel.getUniqueId(), "超时取消");
                    continue;
                }
                Result<?> cancelResult = null;
                try {
                    // 发送取消消息
                    cancelResult = messageService.sendWheelCancelMsgAndSaveCancelUniqId(cluster, topic,
                            wheelMessageCancel.getUniqueId(), wheelMessageCancel.getBrokerName(),
                            wheelMessageCancel.getDeliverTime());
                } catch (Exception e) {
                    logger.error("send cancel wheel msg failed, auditWheelMessageCancel:{}", wheelMessageCancel, e);
                    cancelResult = Result.getErrorResult(Status.REQUEST_ERROR, e);
                }
                if (cancelResult.isNotOK()) {
                    wheelCancelMessageVo.incrFailedCancelMsgNum();
                    wheelCancelMessageVo.addDetail(wheelMessageCancel.getUniqueId(), "发送取消消息失败");
                    continue;
                }
            } else {
                // 如果出现数据库插入失败或异常情况回滚了，但取消消息发送成功，但是数据库中没有记录，此时需要补偿
                try {
                    cancelUniqIdService.save(topic.getId(), wheelMessageCancel.getUniqueId());
                } catch (Exception e) {
                    logger.error("save cancel uniqId failed, auditWheelMessageCancel:{}", wheelMessageCancel, e);
                }
            }
            wheelCancelMessageVo.incrSuccessCancelMsgNum();
            wheelCancelMessageVo.addDetail(wheelMessageCancel.getUniqueId(), "取消成功");
        }
        if (wheelCancelMessageVo.sendAllOk()) {
            return Result.getResult(wheelCancelMessageVo);
        } else {
            return Result.getResult(Status.AUDIT_MESSAGE_NOT_SEND_OK).setResult(wheelCancelMessageVo);
        }
    }

    private Result<?> getAuditStatusError(int status) {
        return Result.getResult(Status.WEB_ERROR).setMessage("状态：" + StatusEnum.getNameByStatus(status));
    }
}
