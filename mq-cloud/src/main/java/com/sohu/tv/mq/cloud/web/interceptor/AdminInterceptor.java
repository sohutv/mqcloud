package com.sohu.tv.mq.cloud.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.common.util.WebUtil;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * admin权限验证
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月12日
 */
@Component
public class AdminInterceptor extends AuthInterceptor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        boolean pass = super.preHandle(request, response, handler);
        if (pass) {
            if (WebUtil.getAttribute(request, "aosUser") != null) {
                WebUtil.print(response, Result.getJsonResult(Status.PERMISSION_DENIED_ERROR));
                logger.warn("{}, aosUser:{} acess:{}", Status.PERMISSION_DENIED_ERROR.getValue(), WebUtil.getAttribute(request, "aosUser"), request.getRequestURL());
                return false;
            }
            UserInfo userInfo = (UserInfo) WebUtil.getAttribute(request, UserInfo.USER_INFO);
            if(userInfo.getUser().isAdmin()) {
                return true;
            } else {
                WebUtil.print(response, Result.getJsonResult(Status.PERMISSION_DENIED_ERROR));
                logger.warn("{}, user:{} acess:{}", Status.PERMISSION_DENIED_ERROR.getValue(), userInfo, request.getRequestURL());
            }
        }
        return false;
    }
}
