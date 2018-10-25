package com.sohu.tv.mq.cloud.web.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 用户解析
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月12日
 */
public interface UserInfoParser {
    
    /**
     * 从request中解析用户名
     * @param request
     * @return UserInfo
     */
    UserInfo pasrse(HttpServletRequest request, HttpServletResponse response);
}
