package com.sohu.tv.mq.cloud.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserConsumer;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.AuditService;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.service.UserConsumerService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.service.VerifyDataService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

@Controller
@RequestMapping("/userConsumer")
public class UserConsumerController {

    @Autowired
    private UserConsumerService userConsumerService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ConsumerService consumerService;

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
    public Result<?> delete(UserInfo userInfo, @RequestParam(value = "uid", defaultValue = "-1") long uid,
            @RequestParam(value = "cid", defaultValue = "-1") long cid) {
        // 校验
        Result<?> isExist = verifyDataService.verifyDeleteRecordUserConsumerIsExist(uid, cid);
        if (isExist.getStatus() != Status.OK.getKey()) {
            return isExist;
        }
        Result<List<UserConsumer>> userConsumerResult = userConsumerService.queryUserConsumer(uid, cid);
        if (userConsumerResult.isNotOK()) {
            return userConsumerResult;
        }
        // 获取topic,从userConsumerResult里面只取一个结果，后面会在关联关系时做限制，一个用户只能和一个consumerGroup关联
        Result<Topic> topicResult = topicService.queryTopic(userConsumerResult.getResult().get(0).getTid());
        if (topicResult.isNotOK()) {
            return Result.getWebResult(topicResult);
        }
        // 获取用户详细信息
        Result<User> userResult = userService.query(uid);
        if (userResult.isNotOK()) {
            return Result.getWebResult(userResult);
        }
        // 获取consumer详细信息
        Result<Consumer> consumerResult = consumerService
                .queryById(userConsumerResult.getResult().get(0).getConsumerId());
        if (consumerResult.isNotOK()) {
            return Result.getWebResult(consumerResult);
        }
        // 构造审核记录
        Audit audit = new Audit();
        audit.setType(TypeEnum.DELETE_USERCONSUMER.getType());
        audit.setUid(userInfo.getUser().getId());
        // 保存记录
        Result<?> result = auditService.saveAuditAndUserConsumerDelete(audit,
                userConsumerResult.getResult().get(0).getId(), consumerResult.getResult().getName(),
                topicResult.getResult().getName(), uid);
        if (result.isOK()) {
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.DELETE_USERCONSUMER,
                    userResult.getResult().notBlankName() + "与" + consumerResult.getResult().getName() + "的关联关系");
        }
        return Result.getWebResult(result);

    }
}
