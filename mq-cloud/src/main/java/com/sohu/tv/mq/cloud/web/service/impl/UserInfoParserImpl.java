package com.sohu.tv.mq.cloud.web.service.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.WebUtil;
import com.sohu.tv.mq.cloud.web.service.UserInfoParser;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
/**
 * 解析用户名
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月12日
 */
@Component
public class UserInfoParserImpl implements UserInfoParser {

    @Autowired
    private UserService userService;
    
    @Override
    public UserInfo pasrse(HttpServletRequest request, HttpServletResponse response) {
        String ip = WebUtil.getIp(request);
        UserInfo userInfo = new UserInfo();
        userInfo.setIp(ip);
        userInfo.setLoginTime(System.currentTimeMillis());
        
        String email = WebUtil.getEmailAttribute(request);
        if(email == null) {
            return userInfo;
        }
        userInfo.setLoginId(email);
        Result<User> userResult = userService.queryByEmail(email);
        userInfo.setUserResult(userResult);
        return userInfo;
    }
}
