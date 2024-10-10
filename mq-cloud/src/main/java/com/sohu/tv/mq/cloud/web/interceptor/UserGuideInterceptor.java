package com.sohu.tv.mq.cloud.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.sohu.tv.mq.cloud.common.service.LoginService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.common.util.WebUtil;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 用户引导拦截器
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月13日
 */
@Component
public class UserGuideInterceptor extends HandlerInterceptorAdapter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private LoginService loginService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String email = loginService.getLoginEmail(request);
        // 校验邮箱是否合法
        if(!EmailValidator.getInstance().isValid(email)) {
            logger.error("invalid email:{}", email);
            WebUtil.print(response, Result.getJsonResult(Status.NOLOGIN_ERROR));
            return false;
        }
        
        UserInfo userInfo = new UserInfo();
        userInfo.setIp(WebUtil.getIp(request));
        userInfo.setLoginTime(System.currentTimeMillis());
        userInfo.setLoginId(email);
        WebUtil.setAttribute(request, UserInfo.USER_INFO, userInfo);
        return true;
    }
}
