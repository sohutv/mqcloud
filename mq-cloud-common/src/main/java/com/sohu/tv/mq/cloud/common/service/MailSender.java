package com.sohu.tv.mq.cloud.common.service;
/**
 * 报警信息发送
 * 
 * @author yongfeigao
 * @date 2018年10月8日
 */
public interface MailSender {
    /**
     * 发送邮件
     * 
     * @param title 标题
     * @param content 内容
     * @param email xx@xxx.com,xx@xxx.com
     * @return 成功返回true，否则返回false
     */
    public boolean send(String title, String content, String email);

    /**
     * 发送邮件
     * 
     * @param title 标题
     * @param content 内容
     * @param email xx@xxx.com,xx@xxx.com
     * @param timeout 超时毫秒
     * @return 成功返回true，否则返回false
     */
    public boolean send(String title, String content, String email, int timeout);

    /**
     * 发送邮件
     * 
     * @param title 标题
     * @param content 内容
     * @param email xx@xxx.com,xx@xxx.com
     * @param ccEmail xx@xxx.com,xx@xxx.com
     * @param timeout 超时毫秒
     * @return 成功返回true，否则返回false
     */
    public boolean send(String title, String content, String email, String ccEmail, int timeout);
}
