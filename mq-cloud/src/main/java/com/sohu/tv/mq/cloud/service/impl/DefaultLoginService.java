package com.sohu.tv.mq.cloud.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sohu.tv.mq.cloud.common.service.impl.AbstractLoginService;
import com.sohu.tv.mq.cloud.common.util.WebUtil;
/**
 * 默认的登录服务实现，采用用户名密码验证
 * 
 * @author yongfeigao
 * @date 2018年10月9日
 */
public class DefaultLoginService extends AbstractLoginService {

    @Override
    protected void auth(HttpServletRequest request, HttpServletResponse response) {
        try {
            String url = WebUtil.getUrl(request);
            try {
                url = URLEncoder.encode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("url encode:{}", url, e);
            }
            WebUtil.redirect(response, request, "/login?redirect=" + url);
        } catch (IOException e) {
            logger.error("redirect err", e);
        }
    }

    @Override
    protected String getEmail(String ticketKey) {
        return null;
    }

    @Override
    public void init() {
        
    }

}
