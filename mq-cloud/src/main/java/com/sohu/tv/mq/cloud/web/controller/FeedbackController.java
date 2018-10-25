package com.sohu.tv.mq.cloud.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.Feedback;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.FeedbackService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
/**
 * 反馈
 * 
 * @author yongfeigao
 * @date 2018年10月15日
 */
@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;
    
    @Autowired
    private AlertService alertService;

    /**
     * add
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> add(UserInfo userInfo, @RequestParam("content") String content) throws Exception {
        Feedback feedback = new Feedback();
        feedback.setContent(content);
        feedback.setUid(userInfo.getUser().getId());
        Result<?> result = feedbackService.save(feedback);
        alertService.sendMail("MQCloud用户反馈", 
                "用户: <b>" + userInfo.getUser().getEmail() + "</b><br>" +
                "反馈: " + feedback);
        return Result.getWebResult(result);
    }
}
