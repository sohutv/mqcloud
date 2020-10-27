package com.sohu.tv.mq.cloud.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
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
public class MQCloudConfigHelper implements ApplicationEventPublisherAware, CommandLineRunner {

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
    private String repositoryUrl;

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

    // 以下为邮件发送相关
    private String mailHost;

    private int mailPort;

    private String mailProtocol;

    private String mailUsername;

    private String mailPassword;
    // ms
    private int mailTimeout;
    // 是否开启注册
    private Integer isOpenRegister;
    // 忽略的topic
    private String ignoreTopic;
    // 忽略的topic
    private String[] ignoreTopicArray;

    // rocketmq安装文件路径
    private String rocketmqFilePath;

    private String privateKey;

    // rocketmq admin accessKey
    private String adminAccessKey;

    // rocketmq admin secretKey
    private String adminSecretKey;

    // 自动审核类型
    private int[] autoAuditType;

    // 机房
    private List<String> machineRoom;

    // 机房颜色
    private List<String> machineRoomColor;
    
    // 从slave查询消息
    private Boolean queryMessageFromSlave;
    
    // 消费落后多少进行预警,单位byte
    private Long consumeFallBehindSize = 0L;
    
    // 消息类型位置
    private String messageTypeLocation;

    @Autowired
    private CommonConfigService commonConfigService;

    private ApplicationEventPublisher publisher;

    @PostConstruct
    public void init() throws IllegalArgumentException, IllegalAccessException {
        Result<List<CommonConfig>> result = commonConfigService.query();
        if (result.isEmpty()) {
            logger.error("no CommonConfig data found!");
            return;
        }
        List<CommonConfig> commonConfigList = result.getResult();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            CommonConfig commonConfig = findCommonConfig(commonConfigList, field.getName());
            if (commonConfig == null) {
                continue;
            }
            String value = commonConfig.getValue();
            if (value == null) {
                continue;
            }
            Class<?> fieldType = field.getType();
            if (fieldType == String.class) {
                field.set(this, value.trim());
            } else if (fieldType == Integer.class) {
                field.set(this, Integer.valueOf(value));
            } else if (fieldType == Long.class) {
                field.set(this, Long.valueOf(value));
            } else if (fieldType == Boolean.class) {
                field.set(this, Boolean.valueOf(value));
            } else {
                field.set(this, JSON.parseObject(value, fieldType));
            }
        }
        // 发布更新时间
        publisher.publishEvent(new MQCloudConfigEvent());
        logger.info("init ok:{}", this);
    }

    private CommonConfig findCommonConfig(List<CommonConfig> commonConfigList, String key) {
        for (CommonConfig commonConfig : commonConfigList) {
            if (commonConfig.getKey().equals(key)) {
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
    
    public boolean isSohu() {
        return profile.contains("sohu");
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

    public String getTopicLink(long topicId, String linkText) {
        return getHrefLink(getTopicLink(topicId) + "?from=alert", linkText);
    }

    public String getTopicConsumeLink(long topicId) {
        return getTopicLink(topicId) + "?tab=consume";
    }
    
    public String getTopicProduceLink(long topicId, String linkText) {
        return getHrefLink(getTopicLink(topicId) + "?tab=produce", linkText);
    }
    
    public String getTopicConsumeLink(long topicId, String linkText) {
        return getHrefLink(getTopicConsumeLink(topicId) + "&consumer=" + linkText, linkText);
    }
    
    public String getTopicConsumeLink(String topic, String consumer) {
        return getHrefLink(getPrefix() + "topic/detail?topic=" + topic + "&consumer=" + consumer, consumer);
    }

    private String getHrefLink(String link, String linkText) {
        return "<a href='" + link + "'>" + linkText + "</a>";
    }

    public String getAuditLink() {
        return getPrefix() + "admin/audit/list";
    }

    public String getServerLink(String ip) {
        return getHrefLink(getPrefix() + "admin/server/list?ip=" + ip, ip);
    }
    
    public String getBrokerStoreLink(int cid, String ip) {
        return getHrefLink(getPrefix() + "admin/cluster/list?cid=" + cid + "&brokerStoreIp=" + ip, ip);
    }

    public String getNameServerMonitorLink(int cid) {
        return getPrefix() + "admin/nameserver/list?cid=" + cid;
    }

    public String getBrokerMonitorLink(int cid) {
        return getPrefix() + "admin/cluster/list?cid=" + cid;
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

    public Integer getIsOpenRegister() {
        return isOpenRegister;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
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

    public String getMailHost() {
        return mailHost;
    }

    public int getMailPort() {
        return mailPort;
    }

    public String getMailProtocol() {
        return mailProtocol;
    }

    public int getMailTimeout() {
        return mailTimeout;
    }

    public String getMailUsername() {
        return mailUsername;
    }

    public String getMailPassword() {
        return mailPassword;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public void setMailPort(int mailPort) {
        this.mailPort = mailPort;
    }

    public void setMailProtocol(String mailProtocol) {
        this.mailProtocol = mailProtocol;
    }

    public void setMailUsername(String mailUsername) {
        this.mailUsername = mailUsername;
    }

    public void setMailPassword(String mailPassword) {
        this.mailPassword = mailPassword;
    }

    public void setMailTimeout(int mailTimeout) {
        this.mailTimeout = mailTimeout;
    }

    public String getIgnoreTopic() {
        return ignoreTopic;
    }

    public boolean isIgnoreTopic(String topic) {
        if (ignoreTopicArray == null) {
            return false;
        }
        for (int i = 0; i < ignoreTopicArray.length; i++) {
            if (topic.equals(ignoreTopicArray[i])) {
                return true;
            }
        }
        return false;
    }

    public void setIgnoreTopic(String ignoreTopic) {
        this.ignoreTopic = ignoreTopic;
        this.ignoreTopicArray = ignoreTopic.split(",");
    }

    public String getRocketmqFilePath() {
        return rocketmqFilePath;
    }

    public void setRocketmqFilePath(String rocketmqFilePath) {
        this.rocketmqFilePath = rocketmqFilePath;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getAdminAccessKey() {
        return adminAccessKey;
    }

    public String getAdminSecretKey() {
        return adminSecretKey;
    }

    public boolean isAdminAclEnable() {
        return StringUtils.isNotEmpty(adminAccessKey) && StringUtils.isNotEmpty(adminSecretKey);
    }

    public int[] getAutoAuditType() {
        return autoAuditType;
    }

    public List<String> getMachineRoom() {
        return machineRoom;
    }

    public Boolean isQueryMessageFromSlave() {
        if (queryMessageFromSlave == null) {
            return false;
        }
        return queryMessageFromSlave;
    }

    public void setAutoAuditType(int[] autoAuditType) {
        this.autoAuditType = autoAuditType;
    }

    public Long getConsumeFallBehindSize() {
        return consumeFallBehindSize;
    }

    public void setConsumeFallBehindSize(Long consumeFallBehindSize) {
        this.consumeFallBehindSize = consumeFallBehindSize;
    }

    public String getMessageTypeLocation() {
        return messageTypeLocation;
    }

    public String getMachineRoomColor(String room) {
        if (room == null || machineRoom == null) {
            if(machineRoomColor != null) {
                return machineRoomColor.get(0);
            } else {
                return "#95a5a6";
            }
        }
        int roomIndex = machineRoom.indexOf(room);
        if (roomIndex == -1) {
            return machineRoomColor.get(0);
        }
        if (roomIndex < machineRoomColor.size()) {
            return machineRoomColor.get(roomIndex);
        }
        return machineRoomColor.get(0);
    }

    /**
     * 是否是自动审核类型
     * 
     * @param type
     * @return
     */
    public boolean isAutoAuditType(int type) {
        for (int auditType : autoAuditType) {
            if (auditType == type) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }

    @Override
    public void run(String... args) throws Exception {
        publisher.publishEvent(new MQCloudConfigEvent());
    }

    /**
     * 配置事件
     */
    public class MQCloudConfigEvent {

    }
}
