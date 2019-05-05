package com.sohu.tv.mq.cloud.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.AuditService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.service.UserProducerService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.service.VerifyDataService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

@Controller
@RequestMapping("/userProducer")
public class UserProducerController {

    @Autowired
    private UserProducerService userProducerService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserService userService;

    @Autowired
    private VerifyDataService verifyDataService;

    /**
     * 删除user-producerGroup对应关系
     * 
     * @param userInfo
     * @param pid
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Result<?> delete(UserInfo userInfo, @RequestParam(value = "pid", defaultValue = "-1") long pid) {
        // 校验
        Result<?> isExist = verifyDataService.verifyDeleteRecordUserProducerIsExist(pid);
        if (isExist.getStatus() != Status.OK.getKey()) {
            return isExist;
        }
        Result<UserProducer> currentUserProducer = userProducerService.findUserProducer(pid);
        if (currentUserProducer.isNotOK()) {
            return currentUserProducer;
        }
        // 获取topic
        Result<Topic> topicResult = topicService.queryTopic(currentUserProducer.getResult().getTid());
        if (topicResult.isNotOK()) {
            return Result.getWebResult(topicResult);
        }
        // 获取用户详细信息
        Result<User> userResult = userService.query(currentUserProducer.getResult().getUid());
        if (userResult.isNotOK()) {
            return Result.getWebResult(userResult);
        }
        // 构造审核记录
        Audit audit = new Audit();
        audit.setType(TypeEnum.DELETE_USERPRODUCER.getType());
        audit.setUid(userInfo.getUser().getId());
        // 保存记录
        Result<?> result = auditService.saveAuditAndUserProducerDelete(audit, pid,
                currentUserProducer.getResult().getProducer(), topicResult.getResult().getName(),
                currentUserProducer.getResult().getUid());
        if (result.isOK()) {
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.DELETE_USERPRODUCER,
                    userResult.getResult().notBlankName() + "与" + currentUserProducer.getResult().getProducer() + "的关联关系");
        }
        return Result.getWebResult(result);

    }
}
