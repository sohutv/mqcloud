package com.sohu.tv.mq.cloud.service;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.common.service.MailSender;
import com.sohu.tv.mq.cloud.common.service.SmsSender;
import com.sohu.tv.mq.cloud.util.Jointer;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 报警服务，从配置文件获取配置
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年5月28日
 */
@Service("mqcloudAlertService")
@ConfigurationProperties("eureka.instance.metadata-map")
public class AlertService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

    @Autowired
    private Configuration configuration;

    @Autowired
    private UserWarnService userWarnService;

    /**
     * 发送审核邮件
     */
    public boolean sendAuditMail(User user, TypeEnum type, String content) {
        String username = user.getName();
        if (StringUtils.isBlank(username)) {
            username = user.getEmail();
        }
        String title = "MQCloud申请";
        if (mqCloudConfigHelper.isLocal()) {
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
        if (mailSender == null) {
            return false;
        }
        return mailSender.send(title, content, getDevelopers());
    }

    /**
     * 发送邮件(同时抄送管理员)
     * 
     * @param title 标题
     * @param content 内容
     * @param email 收件人
     * @return 成功返回true，否则返回false
     */
    public boolean sendMail(String title, String content, String email) {
        if (mailSender == null) {
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
    public boolean sendPhone(String title, String message, Collection<String> phones) {
        if (smsSender == null) {
            logger.info("smsSender is null, title:{}, message:{}, phones:{}", title, message, phones);
            return false;
        }
        if (phones == null || phones.size() == 0) {
            return false;
        }
        for (String phone : phones) {
            sendPhone(title, message, phone);
        }
        return true;
    }

    /**
     * 发送手机预警
     * 
     * @param title
     * @param message
     * @param phone
     * @return
     */
    public boolean sendPhone(String title, String message, String phone) {
        return smsSender.send("MQCloud" + title + "预警:" + message, phone);
    }

    /**
     * 发送报警邮件
     * 
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
     * 
     * @param email 接收人列表
     * @param title 标题
     * @param content 内容
     * @return 成功返回true，否则返回false
     */
    public boolean sendWarnMail(String email, String flag, String content, int retry) {
        if (retry <= 0) {
            retry = 1;
        }
        for (int i = 0; i < retry; ++i) {
            boolean ok = sendWarnMailInternal(email, flag, content);
            if (ok) {
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
     * 
     * @param email 接收人列表
     * @param title 标题
     * @param content 内容
     * @return 成功返回true，否则返回false
     */
    private boolean sendWarnMailInternal(String email, String flag, String content) {
        String title = "MQCloud " + flag + "预警";
        if (mqCloudConfigHelper.isLocal()) {
            title = "local-" + title;
        }
        if (StringUtils.isBlank(email)) {
            return sendMail(title, content);
        } else {
            if (mailSender == null) {
                return false;
            }
            return mailSender.send(title, content, email, getDevelopers(), 10000);
        }
    }

    public String getDevelopers() {
        String email = userService.queryMonitorEmail();
        if (email == null) {
            return developers;
        }
        return email;
    }

    public void setDevelopers(String developers) {
        this.developers = developers;
    }

    public List<String> getPhones() {
        List<String> dbPhones = userService.queryMonitorPhone();
        if (dbPhones == null) {
            return Arrays.asList(phones.split(","));
        }
        return dbPhones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }

    /**
     * 发送预警
     * 
     * @param users
     * @param warnType
     * @param param
     * @return
     */
    public boolean sendWarn(Collection<User> users, WarnType warnType, Map<String, Object> param) {
        try {
            Template template = configuration.getTemplate("mail/" + warnType.getWarnTemplate());
            StringWriter stringWriter = new StringWriter();
            template.process(param, stringWriter);
            String email = Jointer.BY_COMMA.join(users, u -> u.getEmail());
            String warnContent = stringWriter.toString();
            // 发送邮件预警
            sendWarnMail(email, warnType.getName(), warnContent);
            // 发送手机预警
            sendWarnPhone(users, warnType, param);
            // 保存预警信息
            Object obj = param.get("resource");
            String resource = warnType.getName();
            if (obj != null) {
                resource = obj.toString();
            }
            userWarnService.save(users, warnType, resource, warnContent);
            return true;
        } catch (Exception e) {
            logger.error("sendWarnMail error type:{}, param:{}", warnType, param, e);
        }
        return false;
    }
    
    /**
     * 发送手机预警
     * 
     * @param users
     * @param warnType
     * @param param
     * @return
     */
    private boolean sendWarnPhone(Collection<User> users, WarnType warnType, Map<String, Object> param) {
        // 用户手机预警
        Set<String> userPhones = new HashSet<>();
        // 特殊异常通知管理员
        if (WarnType.BROKER_ERROR == warnType || WarnType.NAMESERVER_ERROR == warnType) {
            List<String> adminPhoneList = getPhones();
            if (adminPhoneList != null) {
                userPhones.addAll(adminPhoneList);
            }
        }
        if (users != null) {
            users.stream().filter(u -> u.receivePhoneNotice() && u.getMobile() != null)
                    .map(u -> u.getMobile())
                    .forEach(m -> userPhones.add(m));
        }
        if (userPhones.size() > 0) {
            return sendWarnPhone(warnType, param, userPhones);
        }
        return false;
    }

    /**
     * 发送手机预警
     * 
     * @param warnType
     * @param param
     * @return
     */
    public boolean sendWarnPhone(WarnType warnType, Map<String, Object> param, Collection<String> phones) {
        try {
            Template template = configuration.getTemplate("phone/" + warnType.getWarnTemplate());
            StringWriter stringWriter = new StringWriter();
            param.put("newLine", "\n");
            template.process(param, stringWriter);
            String warnContent = stringWriter.toString();
            sendPhone(warnType.getName(), warnContent, phones);
            return true;
        } catch (Exception e) {
            logger.warn("sendWarnPhone error type:{}, param:{}, error:{}", warnType, param, e.toString());
        }
        return false;
    }
}
