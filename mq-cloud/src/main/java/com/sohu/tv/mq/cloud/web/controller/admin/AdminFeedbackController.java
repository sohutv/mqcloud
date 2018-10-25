package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sohu.tv.mq.cloud.bo.Feedback;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.service.FeedbackService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.Result;
/**
 * 用户反馈
 * 
 * @author yongfeigao
 * @date 2018年9月18日
 */
@Controller
@RequestMapping("/admin/feedback")
public class AdminFeedbackController extends AdminViewController {
    
    @Autowired
    private FeedbackService feedbackService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取notice列表
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    public String list(Map<String, Object> map) throws Exception {
        setView(map, "list");
        Result<List<Feedback>> feedbackListResult = feedbackService.queryAll();
        if(feedbackListResult.isNotEmpty()) {
            Set<Long> uidSet = new HashSet<Long>();
            for(Feedback feedback : feedbackListResult.getResult()) {
                uidSet.add(feedback.getUid());
            }
            Result<List<User>> userListResult = userService.query(uidSet);
            if(userListResult.isNotEmpty()) {
                assginUser(feedbackListResult.getResult(), userListResult.getResult());
            }
        }
        setResult(map, feedbackListResult);
        return view();
    }
    
    /**
     * 将user赋给feedback
     * @param feedbackList
     * @param userList
     */
    private void assginUser(List<Feedback> feedbackList, List<User> userList) {
        for(Feedback feedback : feedbackList) {
            for(User user : userList) {
                if(feedback.getUid() == user.getId()) {
                    feedback.setUser(user);
                    break;
                }
            }
        }
    }
    
    @Override
    public String viewModule() {
        return "feedback";
    }

}
