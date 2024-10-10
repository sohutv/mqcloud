package com.sohu.tv.mq.cloud.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.sohu.tv.mq.cloud.common.service.LoginService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.common.util.WebUtil;
import com.sohu.tv.mq.cloud.web.service.UserInfoParser;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 权限验证
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月12日
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private UserInfoParser userInfoParser;
    
    @Autowired
    private LoginService loginService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        boolean ok = loginService.login(request, response);
        if(!ok) {
            return false;
        }
        // 解析用户信息
        UserInfo userInfo = userInfoParser.pasrse(request, response);
        // 用户登录票据不存在
        if(userInfo.getLoginId() == null) {
            logger.warn("cannot parse userinfo, userInfo:{}", userInfo);
            WebUtil.print(response, Result.getJsonResult(Status.NOLOGIN_ERROR));
            return false;
        }
        if(!userInfo.isOK()) {
            // 用户数据不OK，需要判断是否是异常导致的，才能确定是否真实存在
            Exception exception = userInfo.getError();
            if(exception == null) {
                // 用户真实不存在，需要走引导流程
                WebUtil.redirect(response, request, "/user/guide");
            } else {
                // 给出异常提示信息
                logger.error("userinfo fetch err, userInfo:{}, err:{}", userInfo, exception.getMessage());
                WebUtil.print(response, Result.getJsonResult(Result.getWebErrorResult(exception)));
            }
            return false;
        }
        WebUtil.setAttribute(request, UserInfo.USER_INFO, userInfo);
        if (mqCloudConfigHelper.isSohu()) {
            WebUtil.setAttribute(request, "sohu", "true");
            if (mqCloudConfigHelper.isTestSohu()) {
                WebUtil.setAttribute(request, "testSohu", "true");
            }
        }
        return true;
    }

    public void setUserInfoParser(UserInfoParser userInfoParser) {
        this.userInfoParser = userInfoParser;
    }
    
}
