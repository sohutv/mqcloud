package com.sohu.tv.mq.cloud.processor;

import javax.servlet.ServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.sohu.tv.mq.cloud.util.WebUtil;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * userinfo参数解析
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月12日
 */
public class UserInfoMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(UserInfo.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        UserInfo userInfo = (UserInfo) WebUtil.getAttribute((ServletRequest) webRequest.getNativeRequest(),
                UserInfo.USER_INFO);
        if (userInfo != null) {
            return userInfo;
        }
        throw new MissingServletRequestPartException(UserInfo.USER_INFO);
    }
}
