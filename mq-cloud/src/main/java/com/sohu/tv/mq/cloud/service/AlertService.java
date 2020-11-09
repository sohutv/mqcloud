package com.sohu.tv.mq.cloud.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.common.service.MailSender;
import com.sohu.tv.mq.cloud.common.service.SmsSender;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;

/**
 * 报警服务，从配置文件获取配置
 * @Description: 
 * @author yongfeigao
 * @date 2018年5月28日
 */
@Service("mqcloudAlertService")
@ConfigurationProperties("eureka.instance.metadataMap")
public class AlertService {
    
    // email地址
    private String developers;
    
    // 电话
    private String phones;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @Autowired(required = false)
    private MailSender mailSender;
    
    @Autowired(required = false)
    private SmsSender smsSender;
    
    /**
     * 发送审核邮件
     */
    public boolean sendAuditMail(User user, TypeEnum type, String content) {
        String username = user.getName();
        if (StringUtils.isBlank(username)) {
            username = user.getEmail();
        }
        String title = "MQCloud申请";
        if(mqCloudConfigHelper.isLocal()) {
            title = "local-" + title;
        }
        return sendMail(title, username + "申请" + type.getName() + " " + content + 
                ", <a href='" + mqCloudConfigHelper.getAuditLink() + "'>去审核</a>");
    }
    
    /**
     * 发送邮件
     * 
     * @param title 标题
     * @param content 内容
     * @return 成功返回true，否则返回false
     */
    public boolean sendMail(String title, String content) {
        if(mailSender == null) {
            return false;
        }
        return mailSender.send(title, content, getDevelopers());
    }
    
    /**
     * 发送邮件(同时抄送管理员)
     * @param title 标题
     * @param content 内容
     * @param email 收件人
     * @return 成功返回true，否则返回false
     */
    public boolean sendMail(String title, String content, String email) {
        if(mailSender == null) {
            return false;
        }
        return mailSender.send(title, content, email, getDevelopers(), 0);
    }
    
    /**
     * 发送手机报警
     * 
     * @param message 报警信息
     * @return 成功返回true，否则返回false
     */
    public boolean sendPhone(String title, String message) {
        if(smsSender == null) {
            return false;
        }
        List<String> ps = getPhones();
        if(ps == null) {
            return false;
        }
        for(String phone : ps) {
            smsSender.send(title + "预警]" + message, phone);
        }
        return true;
    }
    
    /**
     * 发送报警邮件
     * @param email 接收人列表
     * @param title 标题
     * @param content 内容
     * @return 成功返回true，否则返回false
     */
    public boolean sendWarnMail(String email, String flag, String content) {
        return sendWarnMail(email, flag, content, 3);
    }
    
    /**
     * 发送报警邮件
     * @param email 接收人列表
     * @param title 标题
     * @param content 内容
     * @return 成功返回true，否则返回false
     */
    public boolean sendWarnMail(String email, String flag, String content, int retry) {
        if(retry <= 0) {
            retry = 1;
        }
        for(int i = 0; i < retry; ++i) {
            boolean ok = sendWarnMailInternal(email, flag, content);
            if(ok) {
                return true;
            } else {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    /**
     * 发送报警邮件
     * @param email 接收人列表
     * @param title 标题
     * @param content 内容
     * @return 成功返回true，否则返回false
     */
    private boolean sendWarnMailInternal(String email, String flag, String content) {
        String title = "MQCloud " + flag + "预警";
        if(mqCloudConfigHelper.isLocal()) {
            title = "local-" + title;
        }
        if(StringUtils.isBlank(email)) {
            return sendMail(title, content);
        } else {
            if(mailSender == null) {
                return false;
            }
            return mailSender.send(title, content, email, getDevelopers(), 10000);
        }
    }
    
    public String getDevelopers() {
        String email = userService.queryMonitorEmail();
        if(email == null) {
            return developers;
        }
        return email;
    }

    public void setDevelopers(String developers) {
        this.developers = developers;
    }

    public List<String> getPhones() {
        List<String> dbPhones = userService.queryMonitorPhone();
        if(dbPhones == null) {
            return Arrays.asList(phones.split(","));
        }
        return dbPhones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }
}
