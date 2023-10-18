package com.sohu.tv.mq.cloud.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.sohu.tv.mq.cloud.bo.Feedback;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.FeedbackService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

import java.util.Map;

/**
 * 反馈
 * 
 * @author yongfeigao
 * @date 2018年10月15日
 */
@Controller
@RequestMapping("/feedback")
public class FeedbackController extends ViewController {

    private static String FEED_BACK_TITLE = "MQCloud用户反馈";
    
    @Autowired
    private FeedbackService feedbackService;
    
    @Autowired
    private AlertService alertService;

    /**
     * feedback add page
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String add(Map<String, Object> map) throws Exception {
        setView(map, "add", "我要反馈");
        return view();
    }

    /**
     * add
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> add(UserInfo userInfo, @RequestParam("content") String content) throws Exception {
        content = HtmlUtils.htmlEscape(content, "UTF-8");
        Feedback feedback = new Feedback();
        feedback.setContent(content);
        feedback.setUid(userInfo.getUser().getId());
        Result<?> result = feedbackService.save(feedback);
        alertService.sendMail(FEED_BACK_TITLE, "您好!<br> 我们已收到您的反馈，感谢您为MQCloud做出的贡献 !<br>反馈内容如下:<br>" + content,
                userInfo.getUser().getEmail());
        return Result.getWebResult(result);
    }

    @Override
    public String viewModule() {
        return "feedback";
    }
}
