package com.sohu.tv.mq.cloud.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.UserMessage;
import com.sohu.tv.mq.cloud.service.UserMessageService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.MessageVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 用户消息
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
@Controller
@RequestMapping("/message")
public class MessageController extends ViewController {
    
    @Autowired
    private UserMessageService userMessageService;
    
    /**
     * 获取未读消息
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping
    public Result<List<MessageVO>> index(UserInfo userInfo) throws Exception {
        Result<List<UserMessage>> userMessageListResult = userMessageService.queryUnread(userInfo.getUser().getId());
        if(userMessageListResult.isEmpty()) {
            return Result.getResult(Status.NO_RESULT);
        }
        List<UserMessage> userMessageList = userMessageListResult.getResult();
        List<MessageVO> msgList = new ArrayList<>(userMessageList.size());
        for(UserMessage userMessage : userMessageList) {
            MessageVO messageVO = new MessageVO();
            messageVO.setId(userMessage.getId());
            messageVO.setText(userMessage.getMessage());
            messageVO.setReadStatus(userMessage.getStatus());
            msgList.add(messageVO);
        }
        return Result.getResult(msgList);
    }
    
    /**
     * 获取所有消息
     * @return
     * @throws Exception
     */
    @RequestMapping("/all")
    public String all(UserInfo userInfo, Map<String, Object> map) throws Exception {
        Result<List<UserMessage>> userMessageListResult = userMessageService.queryAll(userInfo.getUser().getId());
        setResult(map, userMessageListResult);
        return viewModule() + "/all";
    }
    
    
    /**
     * 标记为已读
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="/read/all", method=RequestMethod.POST)
    public Result<?> readAll(UserInfo userInfo) throws Exception {
        Result<?> result = userMessageService.setToReadByUid(userInfo.getUser().getId());
        return result;
    }
    
    /**
     * 标记为已读
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="/{mid}/read", method=RequestMethod.POST)
    public Result<?> read(UserInfo userInfo, @PathVariable long mid) throws Exception {
        Result<?> result = userMessageService.setToRead(mid, userInfo.getUser().getId());
        return result;
    }

    @Override
    public String viewModule() {
        return "message";
    }
}
