package com.sohu.tv.mq.cloud.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.cloud.bo.CommonConfig;
import com.sohu.tv.mq.cloud.service.CommonConfigService;

/**
 * mqcloud配置
 * 
 * @author yongfeigao
 */
@Component
@ConfigurationProperties(prefix = "mqcloud")
public class MQCloudConfigHelper {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public static final String HTTP_SCHEMA = "http://";
    
    public static final String NMON_ZIP = "nmon.zip";
    
    public static final String ROCKETMQ_FILE = "rocketmq.zip";

    // 应用路径
    @Value("${server.contextPath}")
    private String contextPath;
    
    // 环境
    @Value("${spring.profiles.active:local}")
    private String profile;

    // mqcloud的域名
    private String domain;
    
    // nexus的域名
    private String nexusDomain;

    // 密码助手的key
    private String ciperKey;

    // cas登录返回后的key，用户名密码可以忽略
    private String ticketKey;

    // 服务器 ssh 用户
    private String serverUser;

    // 服务器 ssh 密码
    private String serverPassword;

    // 服务器 ssh 端口
    private Integer serverPort;

    // 服务器 ssh 链接建立超时时间
    private Integer serverConnectTimeout;

    // 服务器 ssh 操作超时时间
    private Integer serverOPTimeout;

    // 运维人员
    private List<Map<String, String>> operatorContact;

    // 特别感谢
    private String specialThx;
    
    // 消息体是对象的topic列表
    private List<String> classList;
    
    // 消息体是map，但是value含有byte[]的topic列表
    private List<String> mapWithByteList;
    
    // 客户端包的artifactId
    private String clientArtifactId;
    
    // 发送者类，用于快速指南里提示
    private String producerClass;
    
    // 消费者类，用于快速指南里提示
    private String consumerClass;
    
    @Autowired
    private CommonConfigService commonConfigService;
    
    @PostConstruct
    public void init() throws IllegalArgumentException, IllegalAccessException {
        Result<List<CommonConfig>> result = commonConfigService.query();
        if(result.isEmpty()) {
            logger.error("no CommonConfig data found!");
            return;
        }
        List<CommonConfig> commonConfigList = result.getResult();
        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            CommonConfig commonConfig = findCommonConfig(commonConfigList, field.getName());
            if(commonConfig == null) {
                continue;
            }
            String value = commonConfig.getValue();
            Class<?> fieldType = field.getType();
            if(fieldType == String.class) {
                field.set(this, value);
            } else if(fieldType == Integer.class) {
                field.set(this, Integer.valueOf(value));
            } else {
                field.set(this, JSON.parseObject(value, fieldType));
            }
        }
        
        logger.info("init ok:{}", this);
    }
    
    private CommonConfig findCommonConfig(List<CommonConfig> commonConfigList, String key) {
        for(CommonConfig commonConfig : commonConfigList) {
            if(commonConfig.getKey().equals(key)) {
                return commonConfig;
            }
        }
        return null;
    }
    
    public boolean isOnline() {
        return "online".equals(profile) || "online-sohu".equals(profile);
    }

    public boolean isLocal() {
        return "local".equals(profile) || "local-sohu".equals(profile);
    }

    public String getEnv() {
        return profile;
    }

    public String getDomain() {
        return domain;
    }
    
    public String getNMONURL() {
        return HTTP_SCHEMA + getDomain() + "/software/" + NMON_ZIP;
    }
    
    public String getRocketMQURL() {
        return HTTP_SCHEMA + getDomain() + "/software/" + ROCKETMQ_FILE;
    }

    public String getCiperKey() {
        return ciperKey;
    }

    public String getTicketKey() {
        return ticketKey;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getTopicLink(long topicId) {
        return getPrefix() + "user/topic/" + topicId + "/detail";
    }

    public String getTopicConsumeLink(long topicId) {
        return getTopicLink(topicId) + "?tab=consume";
    }

    public String getAuditLink() {
        return getPrefix() + "admin/audit/list";
    }

    private String getPrefix() {
        return HTTP_SCHEMA + getDomain() + getContextPath();
    }

    public String getServerUser() {
        return serverUser;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getServerConnectTimeout() {
        return serverConnectTimeout;
    }

    public int getServerOPTimeout() {
        return serverOPTimeout;
    }

    public List<Map<String, String>> getOperatorContact() {
        return operatorContact;
    }

    public String getSpecialThx() {
        return specialThx;
    }

    public List<String> getClassList() {
        return classList;
    }

    public List<String> getMapWithByteList() {
        return mapWithByteList;
    }

    public String getClientArtifactId() {
        return clientArtifactId;
    }

    public String getProducerClass() {
        return producerClass;
    }

    public String getConsumerClass() {
        return consumerClass;
    }

    public String getNexusDomain() {
        return nexusDomain;
    }

    public void setNexusDomain(String nexusDomain) {
        this.nexusDomain = nexusDomain;
    }

    public void setTicketKey(String ticketKey) {
        this.ticketKey = ticketKey;
    }

    public void setClientArtifactId(String clientArtifactId) {
        this.clientArtifactId = clientArtifactId;
    }

    public void setProducerClass(String producerClass) {
        this.producerClass = producerClass;
    }

    public void setConsumerClass(String consumerClass) {
        this.consumerClass = consumerClass;
    }

    @Override
    public String toString() {
        return "MQCloudConfigHelper [contextPath=" + contextPath + ", profile=" + profile + ", domain=" + domain
                + ", nexusDomain=" + nexusDomain + ", ciperKey=" + ciperKey + ", ticketKey=" + ticketKey
                + ", serverUser=" + serverUser + ", serverPassword=" + serverPassword + ", serverPort=" + serverPort
                + ", serverConnectTimeout=" + serverConnectTimeout + ", serverOPTimeout=" + serverOPTimeout
                + ", operatorContact=" + operatorContact + ", specialThx=" + specialThx + ", classList=" + classList
                + ", mapWithByteList=" + mapWithByteList + ", clientArtifactId=" + clientArtifactId + ", producerClass="
                + producerClass + ", consumerClass=" + consumerClass + "]";
    }
}
