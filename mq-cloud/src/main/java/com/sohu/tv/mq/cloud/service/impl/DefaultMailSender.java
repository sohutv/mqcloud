package com.sohu.tv.mq.cloud.service.impl;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.common.service.MailSender;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper.MQCloudConfigEvent;

/**
 * 默认实现
 * 
 * @author yongfeigao
 * @date 2018年10月10日
 */
@Component
public class DefaultMailSender implements MailSender {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    private volatile JavaMailSenderImpl mailSender;

    /**
     * 初始化mail发送
     */
    public void init() {
        if(mqCloudConfigHelper == null) {
            logger.warn("init mail sender err, mqCloudConfigHelper is null!");
            return;
        }
        try {
            JavaMailSenderImpl tempMailSender = new JavaMailSenderImpl();
            tempMailSender.setHost(mqCloudConfigHelper.getMailHost());
            tempMailSender.setPort(mqCloudConfigHelper.getMailPort());
            tempMailSender.setProtocol(mqCloudConfigHelper.getMailProtocol());
            tempMailSender.setUsername(mqCloudConfigHelper.getMailUsername());
            tempMailSender.setPassword(mqCloudConfigHelper.getMailPassword());
            Properties props = new Properties();
            props.put("mail." + mqCloudConfigHelper.getMailProtocol() + ".auth", true);
            props.put("mail." + mqCloudConfigHelper.getMailProtocol() + ".connectiontimeout",
                    mqCloudConfigHelper.getMailTimeout());
            props.put("mail." + mqCloudConfigHelper.getMailProtocol() + ".timeout",
                    mqCloudConfigHelper.getMailTimeout());
            tempMailSender.setJavaMailProperties(props);
            tempMailSender.testConnection();
            // 连接成功赋值
            mailSender = tempMailSender;
            if(logger.isDebugEnabled()) {
                logger.debug("init mail sender ok, mailHost:{}, mailPort:{}, mailProtocol:{}, mailUserName:{}, "
                        + "mailPassword:{}, mailTimeout:{}ms, err:{}",
                        mqCloudConfigHelper.getMailHost(),
                        mqCloudConfigHelper.getMailPort(),
                        mqCloudConfigHelper.getMailProtocol(),
                        mqCloudConfigHelper.getMailUsername(),
                        mqCloudConfigHelper.getMailPassword(),
                        mqCloudConfigHelper.getMailTimeout());
            }
        } catch (Exception e) {
            logger.warn("init mail sender err, mailHost:{}, mailPort:{}, mailProtocol:{}, mailUserName:{}, "
                    + "mailPassword:{}, mailTimeout:{}ms, err:{}",
                    mqCloudConfigHelper.getMailHost(),
                    mqCloudConfigHelper.getMailPort(),
                    mqCloudConfigHelper.getMailProtocol(),
                    mqCloudConfigHelper.getMailUsername(),
                    mqCloudConfigHelper.getMailPassword(),
                    mqCloudConfigHelper.getMailTimeout(),
                    e.getMessage());
        }
    }
    
    /**
     * 配置改变
     */
    @EventListener
    public void configChange(MQCloudConfigEvent mqCloudConfigEvent) {
        init();
    }

    @Override
    public boolean send(String title, String content, String email) {
        return send(title, content, email, null, 0);
    }

    @Override
    public boolean send(String title, String content, String email, int timeout) {
        return send(title, content, email, null, timeout);
    }

    @Override
    public boolean send(String title, String content, String email, String ccEmail, int timeout) {
        if (mailSender == null) {
            logger.warn("mailSender is null, title:{}, email:{}, content:{}", title, email, content);
            return true;
        }
        try {
            MimeMessage mimeMessage = buildMessage(title, content, email, ccEmail);
            if(mimeMessage == null) {
                return false;
            }
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            logger.error("send msg title:{}, email:{}, content:{}, ccEmail:{}, timeout:{}", title, email, content,
                    ccEmail, timeout, e);
        }
        return true;
    }

    /**
     * 构建邮件
     * @param title
     * @param content
     * @param email
     * @param cc
     * @return
     * @throws MessagingException
     */
    private MimeMessage buildMessage(String title, String content, String email, String cc) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
        helper.setFrom(mqCloudConfigHelper.getMailUsername());
        helper.setTo(email.split(","));
        helper.setSubject(title);
        helper.setText(content, true);
        if (cc != null) {
            helper.setCc(cc.split(","));
        }
        return mimeMessage;
    }

    public void setMqCloudConfigHelper(MQCloudConfigHelper mqCloudConfigHelper) {
        this.mqCloudConfigHelper = mqCloudConfigHelper;
    }
}
