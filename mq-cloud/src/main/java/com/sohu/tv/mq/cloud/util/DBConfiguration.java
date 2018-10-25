package com.sohu.tv.mq.cloud.util;

/**
 * 数据源通用配置
 * 
 * @author yongfeigao
 * @date 2017年11月7日
 */
public class DBConfiguration {
    // url
    private String url;
    // username
    private String username;
    // password
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

