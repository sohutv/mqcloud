package com.sohu.tv.mq.cloud.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Audit.StatusEnum;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.admin.AdminMessageController;
import com.sohu.tv.mq.cloud.web.controller.admin.AuditController;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 自动审核
 * 
 * @author yongfeigao
 * @date 2020年2月25日
 */
@Component
public class AutoAuditService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AuditController auditController;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private AdminMessageController adminMessageController;

    /**
     * 自动审核
     */
    public int autoAudit() {
        // 审核量
        int auditCount = 0;
        // 构造查询参数
        Audit auditParam = new Audit();
        auditParam.setStatus(StatusEnum.INIT.getStatus());
        auditParam.setType(-1);

        // 查询审核列表
        Result<List<Audit>> auditListResult = auditService.queryAuditList(auditParam);
        if (auditListResult.isEmpty()) {
            return auditCount;
        }
        // 获取admin
        Result<User> userResult = userService.queryByEmail("admin@admin.com");
        if (userResult.isNotOK()) {
            return auditCount;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserResult(userResult);
        // 循环审核记录
        for (Audit audit : auditListResult.getResult()) {
            // 非自动审核类型跳过
            if (!mqCloudConfigHelper.isAutoAuditType(audit.getType())) {
                continue;
            }
            TypeEnum typeEnum = TypeEnum.getEnumByType(audit.getType());
            long aid = audit.getId();
            try {
                Result<?> result = null;
                switch (typeEnum) {
                    case NEW_TOPIC:
                        result = auditController.createTopic(userInfo, aid, clusterService.getFirstClusterId(), 
                                clusterService.getTraceClusterId());
                        break;
                    case UPDATE_TOPIC:
                        result = auditController.updateTopic(userInfo, aid);
                        break;
                    case DELETE_TOPIC:
                        result = auditController.deleteTopic(userInfo, aid);
                        break;
                    case NEW_CONSUMER:
                        result = auditController.createConsumer(userInfo, aid);
                        break;
                    case DELETE_CONSUMER:
                        result = auditController.deleteConsumer(userInfo, aid);
                        break;
                    case BECOME_ADMIN:
                        result = auditController.becomeAdmin(userInfo, aid);
                        break;
                    case DELETE_USERPRODUCER:
                        result = auditController.deleteUserProducer(userInfo, aid);
                        break;
                    case DELETE_USERCONSUMER:
                        result = auditController.deleteUserConsumer(userInfo, aid);
                        break;
                    case RESET_OFFSET:
                    case RESET_RETRY_OFFSET:
                    case RESET_OFFSET_TO_MAX:
                        result = auditController.resetOffset(userInfo, aid);
                        break;
                    case ASSOCIATE_PRODUCER:
                        result = auditController.associateProducer(userInfo, aid);
                        break;
                    case ASSOCIATE_CONSUMER:
                        result = auditController.associateConsumer(userInfo, aid);
                        break;
                    case RESEND_MESSAGE:
                        result = adminMessageController.resend(userInfo, aid);
                        if (result.isOK()) {
                            result = auditController.resendMessage(userInfo, aid);
                        }
                        break;
                    case UPDATE_TOPIC_TRACE:
                        result = auditController.updateTopicTrace(userInfo, aid, clusterService.getTraceClusterId());
                        break;
                    case BATCH_ASSOCIATE:
                        result = auditController.batchAssociate(userInfo, aid);
                        break;
                    case LIMIT_CONSUME:
                    case PAUSE_CONSUME:
                    case RESUME_CONSUME:
                        result = auditController.updateConsumerConfig(userInfo, aid);
                        break;
                    case UPDATE_TOPIC_TRAFFIC_WARN:
                        result = auditController.updateTopicTrafficWarn(userInfo, aid);
                        break;
                }
                if (result != null) {
                    ++auditCount;
                    logger.info("autoaudit id:{} result:{}", aid, result);
                }
            } catch (Exception e) {
                logger.error("audit type:{} id:{} error", typeEnum, aid, e);
                ++auditCount;
            }
        }
        return auditCount;
    }

}
