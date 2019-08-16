package com.sohu.tv.mq.cloud.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.common.service.AlertMessageSender;
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
    private AlertMessageSender alertMessageSender;
    
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
        if(alertMessageSender == null) {
            return false;
        }
        return alertMessageSender.sendMail(title, content, getDevelopers());
    }
    
    /**
     * 发送邮件(同时抄送管理员)
     * @param title 标题
     * @param content 内容
     * @param email 收件人
     * @return 成功返回true，否则返回false
     */
    public boolean sendMail(String title, String content, String email) {
        if(alertMessageSender == null) {
            return false;
        }
        return alertMessageSender.sendMail(title, content, email, getDevelopers(), 0);
    }
    
    /**
     * 发送手机报警
     * 
     * @param message 报警信息
     * @return 成功返回true，否则返回false
     */
    public boolean sendPhone(String message) {
        if(alertMessageSender == null) {
            return false;
        }
        return alertMessageSender.sendPhone(message, getPhones());
    }
    
    /**
     * 发送报警邮件
     * @param email 接收人列表
     * @param title 标题
     * @param content 内容
     * @return 成功返回true，否则返回false
     */
    public boolean sendWanMail(String email, String flag, String content) {
        return sendWanMail(email, flag, content, 3);
    }
    
    /**
     * 发送报警邮件
     * @param email 接收人列表
     * @param title 标题
     * @param content 内容
     * @return 成功返回true，否则返回false
     */
    public boolean sendWanMail(String email, String flag, String content, int retry) {
        if(retry <= 0) {
            retry = 1;
        }
        for(int i = 0; i < retry; ++i) {
            boolean ok = sendWanMailInternal(email, flag, content);
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
    private boolean sendWanMailInternal(String email, String flag, String content) {
        String title = "MQCloud" + flag + "预警";
        if(mqCloudConfigHelper.isLocal()) {
            title = "local-" + title;
        }
        if(StringUtils.isBlank(email)) {
            return sendMail(title, content);
        } else {
            if(alertMessageSender == null) {
                return false;
            }
            return alertMessageSender.sendMail(title, content, email, getDevelopers(), 10000);
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

    public String getPhones() {
        return phones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }
}
