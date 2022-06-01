package com.sohu.tv.mq.cloud.bo;

import org.apache.rocketmq.remoting.protocol.LanguageCode;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: 管理后台语言版本信息展示类
 * @date 2022/5/5 16:51
 */
public class AdminLanguage {

    private byte code;

    private String language;

    public AdminLanguage() {
    }

    public AdminLanguage(LanguageCode languageCode) {
        this.code = languageCode.getCode();
        this.language = languageCode.name();
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
