package com.sohu.tv.mq.cloud.common.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录服务
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月31日
 */
public interface LoginService {

    /**
     * 获取登录的email
     * 
     * @param request
     * @return
     */
    public String getLoginEmail(HttpServletRequest request);

    /**
     * 登录
     * 
     * @param request
     * @param response
     * @return
     */
    public boolean login(HttpServletRequest request, HttpServletResponse response);
}
