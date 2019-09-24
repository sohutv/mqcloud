package com.sohu.tv.mq.cloud.common.service;
/**
 * 短消息发送
 * 
 * @author yongfeigao
 * @date 2019年9月10日
 */
public interface SmsSender {

    /**
     * 发送手机报警
     * 
     * @param message 报警信息
     * @param phone 报警电话
     * @return 成功返回true，否则返回false
     */
    public boolean send(String message, String phone);

    /**
     * 发送手机报警
     * 
     * @param message 报警信息
     * @param phone 报警电话
     * @param timeout 超时毫秒
     * @return 成功返回true，否则返回false
     */
    public boolean send(String message, String phone, int timeout);
}
