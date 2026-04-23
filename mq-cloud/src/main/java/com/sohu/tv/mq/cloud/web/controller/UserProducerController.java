package com.sohu.tv.mq.cloud.web.controller;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
        Result<UserProducer> userProducerResult = userProducerService.findUserProducer(pid);
        if (userProducerResult.isNotOK()) {
            return userProducerResult;
        }
        UserProducer userProducer = userProducerResult.getResult();
        // 获取topic
        Result<Topic> topicResult = topicService.queryTopic(userProducer.getTid());
        if (topicResult.isNotOK()) {
            return Result.getWebResult(topicResult);
        }
        // 获取用户详细信息
        Result<User> userResult = userService.query(userProducer.getUid());
        if (userResult.isNotOK()) {
            return Result.getWebResult(userResult);
        }
        Result<?> producerDeletable = userProducerService.checkProducerDeletable(userProducer);
        if (producerDeletable.isNotOK()) {
            return producerDeletable;
        }
        // 构造审核记录
        Audit audit = new Audit();
        audit.setType(TypeEnum.DELETE_USERPRODUCER.getType());
        audit.setUid(userInfo.getUser().getId());
        // 保存记录
        Result<?> result = auditService.saveAuditAndUserProducerDelete(audit, pid,
                userProducerResult.getResult().getProducer(), topicResult.getResult().getName(),
                userProducerResult.getResult().getUid());
        if (result.isOK()) {
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.DELETE_USERPRODUCER,
                    userResult.getResult().notBlankName() + "与" + userProducerResult.getResult().getProducer() + "的关联关系");
        }
        return Result.getWebResult(result);

    }
}
