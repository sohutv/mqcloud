package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Audit.StatusEnum;
import com.sohu.tv.mq.cloud.bo.AuditResendMessage;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.service.AuditResendMessageService;
import com.sohu.tv.mq.cloud.service.AuditService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.MessageService;
import com.sohu.tv.mq.cloud.service.TopicService;
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
    private MessageService messageService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TopicService topicService;

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
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
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
        String consumer = consumerResult.getResult().getName();

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
            if (consumerResult.getResult().isClustering()) {
                sendResult = messageService.resend(cluster, topic, msg.getMsgId(), consumer);
            } else {
                sendResult = messageService.resendDirectly(cluster, msg.getMsgId(), consumer);
            }
            int status = AuditResendMessage.StatusEnum.SUCCESS.getStatus();
            if (sendResult.isNotOK()) {
                logger.warn("resendMessage cluster:{} topic:{} consumer:{} msgId:{} err:{}", cluster, topic, consumer, 
                        msg.getMsgId(), sendResult);
                status = AuditResendMessage.StatusEnum.FAILED.getStatus();
                resendMessageVO.incrFailed();
            } else {
                resendMessageVO.incrSuccess();
            }
            Result<Integer> updateResult = auditResendMessageService.update(aid, msg.getMsgId(), status);
            if (updateResult.isNotOK()) {
                resendMessageVO.incrStatusUpdatedFailed();
                logger.warn("resendMessage cluster:{} topic:{} consumer:{} msgId:{} update not ok :{}", cluster, topic,
                        consumer, msg.getMsgId(), updateResult);
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
        if (StatusEnum.INIT.getStatus() != audit.getStatus()) {
            return Result.getResult(Status.PARAM_ERROR);
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
        String consumer = consumerResult.getResult().getName();

        // 统计状态
        ResendMessageVO resendMessageVO = new ResendMessageVO();
        resendMessageVO.setTotal(1);
        if(AuditResendMessage.StatusEnum.SUCCESS.getStatus() != auditResendMessage.getStatus()) {
            Result<?> sendResult;
            if (consumerResult.getResult().isClustering()) {
                sendResult = messageService.resend(cluster, topic, msgId, consumer);
            } else {
                sendResult = messageService.resendDirectly(cluster, msgId, consumer);
            }
            int status = AuditResendMessage.StatusEnum.SUCCESS.getStatus();
            if (sendResult.isNotOK()) {
                logger.warn("resendMessage cluster:{} topic:{} consumer:{} msgId:{} err:{}", cluster, topic, consumer,
                        msgId, sendResult);
                status = AuditResendMessage.StatusEnum.FAILED.getStatus();
                resendMessageVO.incrFailed();
            } else {
                resendMessageVO.incrSuccess();
            }
            Result<Integer> updateResult = auditResendMessageService.update(aid, msgId, status);
            if (updateResult.isNotOK()) {
                logger.warn("resendMessage cluster:{} topic:{} consumer:{} msgId:{} update not ok :{}", cluster, topic,
                        consumer, msgId, updateResult);
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
}
