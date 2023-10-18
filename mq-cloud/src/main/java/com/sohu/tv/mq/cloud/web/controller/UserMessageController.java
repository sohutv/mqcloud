package com.sohu.tv.mq.cloud.web.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.UserMessage;
import com.sohu.tv.mq.cloud.service.UserMessageService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 用户消息
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
@Controller
@RequestMapping("/user/message")
public class UserMessageController extends ViewController {
    
    @Autowired
    private UserMessageService userMessageService;
    
    /**
     * 获取未读消息数量
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/count")
    public Result<?> index(UserInfo userInfo) throws Exception {
        Result<Integer> countResult = userMessageService.queryUnread(userInfo.getUser().getId());
        return Result.getWebResult(countResult);
    }
    
    /**
     * 获取所有消息
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    public String list(UserInfo userInfo, @Valid PaginationParam paginationParam, Map<String, Object> map)
            throws Exception {
        // 设置返回视图
        setView(map, "list", "我的消息");
        // 设置分页参数
        setPagination(map, paginationParam);
        // 获取警告数量
        long uid = userInfo.getUser().getId();
        Result<Integer> countResult = userMessageService.queryCount(uid);
        if (!countResult.isOK()) {
            return view();
        }
        paginationParam.caculatePagination(countResult.getResult());
        Result<List<UserMessage>> userMessageListResult = userMessageService.queryList(uid, paginationParam.getBegin(),
                paginationParam.getNumOfPage());
        setResult(map, userMessageListResult);
        return view();
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
        return "userMessage";
    }
}
