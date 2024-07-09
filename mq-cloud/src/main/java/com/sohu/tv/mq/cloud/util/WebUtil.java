package com.sohu.tv.mq.cloud.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * web相关工具
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月12日
 */
public class WebUtil {

    public static final String LOGIN_TOKEN = "TOKEN";

    public static final Long ONE_DAY = 24 * 60 * 60 * 1000L;

    public static final Long ONE_HOUR = 60 * 60 * 1000L;

    public static final Long ONE_MINUTE = 60 * 1000L;

    /**
     * 从request中获取客户端ip
     * 
     * @param request
     * @return
     */
    public static String getIp(ServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        String addr = getHeaderValue(req, "X-Forwarded-For");
        if (StringUtils.isNotEmpty(addr) && addr.contains(",")) {
            addr = addr.split(",")[0];
        }
        if (StringUtils.isEmpty(addr)) {
            addr = getHeaderValue(req, "X-Real-IP");
        }
        if (StringUtils.isEmpty(addr)) {
            addr = req.getRemoteAddr();
        }
        return addr;
    }
    
    /**
     * 获取请求的完整url
     * @param request
     * @return
     */
    public static String getUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if(queryString != null) {
            url += "?" + request.getQueryString();
        }
        return url;
    }
    
    /**
     * 获取ServletRequest header value
     * @param request
     * @param name
     * @return
     */
    public static String getHeaderValue(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        if(v == null) {
            return null;
        }
        return v.trim();
    }
    
    /**
     * 从request属性中获取对象
     * @param request
     * @return
     */
    public static void setEmailAttribute(ServletRequest request, String email) {
        request.setAttribute("email", email);
    }
    
    /**
     * 从request属性中获取对象
     * @param request
     * @return
     */
    public static String getEmailAttribute(ServletRequest request) {
        Object email = request.getAttribute("email");
        if(email == null) {
            return null;
        }
        return email.toString();
    }
    
    /**
     * 从request属性中获取对象
     * @param request
     * @return
     */
    public static void setAttribute(ServletRequest request, String name, Object obj) {
        request.setAttribute(name, obj);
    }
    
    /**
     * 设置对象到request属性中
     * @param request
     * @return
     */
    public static Object getAttribute(ServletRequest request, String name) {
        return request.getAttribute(name);
    }
    
    /**
     * 输出内容到页面
     * @param response
     * @param result
     * @throws IOException
     */
    public static void print(HttpServletResponse response, String result) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(result);
        out.flush();
        out.close();
        out = null;
    }
    
    /**
     * 获取登录的cookie的值
     * 
     * @param request
     * @return
     */
    public static String getLoginCookieValue(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, LOGIN_TOKEN);
        if(cookie != null) {
            return cookie.getValue();
        }
        return null;
    }
    
    /**
     * 获取登录的cookie
     * 
     * @param request
     * @return
     */
    public static Cookie getLoginCookie(HttpServletRequest request) {
        return WebUtils.getCookie(request, LOGIN_TOKEN);
    }

    /**
     * 设置登录的cookie
     * 
     * @param request
     */
    public static void setLoginCookie(HttpServletResponse response, String value) {
        Cookie cookie = new Cookie(LOGIN_TOKEN, value);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 移除登录的cookie
     * 
     * @param request
     */
    public static void deleteLoginCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(LOGIN_TOKEN, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
    
    /**
     * 跳转
     * @param response
     * @param request
     * @param path
     * @throws IOException 
     */
    public static void redirect(HttpServletResponse response, HttpServletRequest request, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }
    
    /**
     * count格式化
     * @param value
     * @return
     */
    public static String countFormat(long value) {
        if (value >= 100000000) {
            return format(value / 100000000d) + "亿";
        }
        if (value >= 10000) {
            return format(value / 10000d) + "万";
        }
        return format(value);
    }
    
    /**
     * size格式化
     * @param value
     * @return
     */
    public static String sizeFormat(long value) {
        if (value >= 1099511627776L) {
            return format(value / 1099511627776d) + "T";
        }
        if (value >= 1073741824) {
            return format(value / 1073741824d) + "G";
        }
        if (value >= 1048576) {
            return format(value / 1048576d) + "M";
        }
        if (value >= 1024) {
            return format(value / 1024d) + "K";
        }
        return format(value) + "B";
    }

    public static String format(double value) {
        // 小数点后1位四舍五入
        long v = Math.round(value * 10);
        if (v % 10 == 0) {
            return String.valueOf(v / 10);
        }
        return String.valueOf(v / 10.0);
    }

    /**
     * 时间格式化
     *
     * @param timeInMills
     * @return
     */
    public static String timeFormat(long timeInMills) {
        StringBuilder builder = new StringBuilder();
        long day = timeInMills / ONE_DAY;
        if (day > 0) {
            builder.append(day).append("天");
        }
        long hour = (timeInMills % ONE_DAY) / ONE_HOUR;
        if (hour > 0) {
            builder.append(hour).append("时");
        }
        long minute = (timeInMills % ONE_HOUR) / ONE_MINUTE;
        if (minute > 0) {
            builder.append(minute).append("分");
        }
        // 一般情况不用精确到秒
        if (builder.length() > 0) {
            return builder.toString();
        }
        long second = (timeInMills % ONE_MINUTE) / 1000;
        return builder.append(second).append("秒").toString();
    }
}
