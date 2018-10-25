package com.sohu.tv.mq.cloud.service.impl;

import com.sohu.tv.mq.cloud.common.service.AlertMessageSender;
/**
 * 空实现
 * 
 * @author yongfeigao
 * @date 2018年10月10日
 */
public class DefaultAlertMessageSender implements AlertMessageSender {

    @Override
    public boolean sendMail(String title, String content, String email) {
        return true;
    }

    @Override
    public boolean sendMail(String title, String content, String email, int timeout) {
        return true;
    }

    @Override
    public boolean sendMail(String title, String content, String email, String ccEmail, int timeout) {
        return true;
    }

    @Override
    public boolean sendPhone(String message, String phone) {
        return true;
    }

    @Override
    public boolean sendPhone(String message, String phone, int timeout) {
        return true;
    }

}
