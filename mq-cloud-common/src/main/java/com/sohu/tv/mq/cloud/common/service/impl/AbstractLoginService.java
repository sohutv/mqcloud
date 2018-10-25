package com.sohu.tv.mq.cloud.common.service.impl;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.tv.mq.cloud.common.service.LoginService;
import com.sohu.tv.mq.cloud.common.util.CipherHelper;
import com.sohu.tv.mq.cloud.common.util.WebUtil;

/**
 * 登录服务
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月31日
 */
public abstract class AbstractLoginService implements LoginService {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private CipherHelper cipherHelper;
    
    // 是否在线
    private boolean online;

    // cas外部登录后，返回的key，可以不用
    private String ticketKey;

    /**
     * 获取登录的email
     * 
     * @param request
     * @return
     */
    public String getLoginEmail(HttpServletRequest request) {
        String email = WebUtil.getLoginCookieValue(request);
        if (email != null) {
            email = cipherHelper.decrypt(email);
            if (StringUtils.isNotBlank(email)) {
                WebUtil.setEmailAttribute(request, email);
                return email;
            }
        }
        return null;
    }

    /**
     * 登录
     * 
     * @param request
     * @param response
     * @return
     */
    public boolean login(HttpServletRequest request, HttpServletResponse response) {
        // 第一步：查看cookie是否存在
        String email = getLoginEmail(request);
        if (email != null) {
            return true;
        }
        // 第二步：查看ticket是否存在
        String ticket = request.getParameter(ticketKey);
        if (StringUtils.isNotBlank(ticket)) {
            String loginId = getEmail(ticket);
            if (StringUtils.isNotBlank(loginId)) {
                // 设置到cookie中
                WebUtil.setLoginCookie(response, cipherHelper.encrypt(loginId));
                try {
                    WebUtil.redirect(response, request, parseRedirect(request));
                } catch (Exception e) {
                    logger.error("redirect err", e);
                }
                return true;
            }
        }
        // 第三步：跳到认证授权页面
        auth(request, response);
        return false;
    }

    private String parseRedirect(HttpServletRequest request) throws Exception {
        try {
            String redirect = request.getParameter("redirect");
            String url = null;
            if (redirect != null) {
                url = URLDecoder.decode(redirect, "UTF-8");
            }
            if (url != null) {
                logger.info("redirect to:" + url);
                return url;
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return "";
    }

    /**
     * 跳到认证页面
     * 
     * @param request
     * @param response
     */
    protected abstract void auth(HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据ticketKey置换email
     * 
     * @param ticketKey
     * @return email
     */
    protected abstract String getEmail(String ticketKey);
    
    /**
     * 子类初始化
     */
    public abstract void init();
    
    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public CipherHelper getCipherHelper() {
        return cipherHelper;
    }

    public void setCipherHelper(CipherHelper cipherHelper) {
        this.cipherHelper = cipherHelper;
    }

    public String getTicketKey() {
        return ticketKey;
    }

    public void setTicketKey(String ticketKey) {
        this.ticketKey = ticketKey;
    }
}
