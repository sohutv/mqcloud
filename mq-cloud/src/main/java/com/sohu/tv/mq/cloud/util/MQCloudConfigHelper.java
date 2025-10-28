package com.sohu.tv.mq.cloud.util;

import com.sohu.tv.mq.cloud.bo.CommonConfig;
import com.sohu.tv.mq.cloud.common.model.BrokerStoreStat;
import com.sohu.tv.mq.cloud.service.CommonConfigService;
import com.sohu.tv.mq.util.CommonUtil;
import com.sohu.tv.mq.util.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    @Value("${server.servlet.context-path}")
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

    private Boolean mailUseSSL;

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

    // rocketmq5安装文件路径
    private String rocketmq5FilePath;

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

    // slave落后多少进行预警，单位byte
    private Long slaveFallBehindSize = 0L;

    // 线程统计支持版本
    private String threadMetricSupportedVersion;
    // 消费失败统计支持版本
    private String consumeFailedMetricSupportedVersion;
    
    private String consumeTimespanMessageSupportedVersion;

    // mq代理服务列表
    private String mqProxyServerString;

    // http协议生产uri前缀
    private String httpProducerUriPrefix;
    // http协议消费uri前缀
    private String httpConsumerUriPrefix;

    // clientGroup ns config
    private Map<String, String> clientGroupNSConfig;

    // 使用旧的请求码的broker
    private Set<String> oldReqestCodeBrokerSet;

    // api接口token用户
    private List<String> apiAuditUserEmail;

    // proxy acl
    private List<Map<String, Object>> proxyAcls;

    // 第一次搜索的最大队列数
    private int maxQueueNumOfFirstSearch = 50;

    // 导出消息的本地存储目录，格式：/mqcloud/download
    private String exportedMessageLocalPath;

    // 导出消息的远程目录，格式：ip:/mqcloud/download
    private String exportedMessageRemotePath;

    // 导出消息的下载url前缀，格式：http://mqcloud.com/download/
    private String exportedMessageDownloadUrlPrefix;

    private Set<String> ignoreErrorProducerSet;

    // 全局顺序topic kv配置
    private Map<String, String> orderTopicKVConfig;

    // rsync配置,包括path, user, password, port, bwlimit, module
    private Map<String, String> rsyncConfig;

    // cluster store警告配置
    private List<Map<String, Integer>> clusterStoreWarnConfig;

    // mqcloud的server列表
    private Set<String> mqcloudServers;

    // 发送预警到外部的消费者
    private Set<String> consumersForSendWarnToOut;

    // 登录类
    private String loginClass;
    // 用户警告服务类
    private String userWarnServiceClass;
    // 邮件类
    private String mailClass;
    // commons object pool2 metrics class
    private String commonsObjectPool2MetricsClass;

    @Autowired
    private CommonConfigService commonConfigService;

    private ApplicationEventPublisher publisher;

	@PostConstruct
    public void init() throws IllegalArgumentException, IllegalAccessException {
        refresh(true);
    }

    public void refresh() throws IllegalArgumentException, IllegalAccessException {
        refresh(false);
    }

    public void refresh(boolean init) throws IllegalArgumentException, IllegalAccessException {
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
            value = value.trim();
            if(value.isEmpty()) {
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
                field.set(this, JSONUtil.parse(value, fieldType));
            }
        }
        // 发布更新时间
        publisher.publishEvent(new MQCloudConfigEvent());
        logger.info("config:{}", init ? this : "refreshed");
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
        return "online".equals(profile) || "online-sohu".equals(profile) || "miniapp-online".equals(profile);
    }

    public boolean isSohu() {
        return profile.contains("sohu");
    }

    public boolean isTestSohu() {
        return profile.contains("test-sohu");
    }

    public boolean isTestOnlineSohu() {
        return profile.contains("test-online-sohu") || profile.contains("local-online-sohu");
    }

    public boolean isMiniApp() {
        return profile.contains("miniapp");
    }

    /**
     * 是否需要监控
     * 测试环境，需要监控；online环境，只监控online集群
     *
     * @return
     */
    public boolean needMonitor(boolean onlineCluster) {
        return !isOnline() || onlineCluster;
    }

    public boolean isLocal() {
        return "local".equals(profile) || "local-sohu".equals(profile);
    }

    public String getProfile() {
        return profile;
    }

    public String getDomain() {
        return domain;
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
        return getHrefLink(getTopicLink(topicId) + "?from=" + linkText, linkText);
    }

    public String getTopicConsumeHrefLink(long topicId) {
        return getTopicLink(topicId) + "?tab=consume";
    }

    public String getTopicProduceLink(long topicId, String linkText) {
        return getHrefLink(getTopicLink(topicId) + "?tab=produce", linkText);
    }

    public String getTopicConsumeHrefLink(long topicId, String linkText) {
        return getTopicConsumeHrefLink(topicId, linkText, 0);
    }

    public String getTopicConsumeHrefLink(long topicId, String linkText, long time) {
        return getHrefLink(getTopicConsumeHref(topicId, linkText, -1, time), linkText);
    }

    public String getTopicConsumeHref(long topicId, String consumer, long consumerId, long time) {
        String link = getTopicConsumeHrefLink(topicId) + "&consumer=" + consumer;
        if (consumerId > 0) {
            link += "&consumerId=" + consumerId;
        }
        if (time > 0) {
            link += "&time=" + time;
        }
        return link;
    }

    public String getTopicConsumeLink(String topic, String consumer) {
        if (CommonUtil.isRetryTopic(topic)) {
            if (consumer == null) {
                consumer = topic.replaceAll(MixAll.RETRY_GROUP_TOPIC_PREFIX, "");
            }
            topic = topic.replaceAll("%", "%25");
        }
        if (consumer != null) {
            return getPrefix() + "topic/detail?topic=" + topic + "&consumer=" + consumer;
        } else {
            return getPrefix() + "topic/detail?topic=" + topic;
        }
    }

    public String getTopicConsumeHrefLink(String topic, String consumer) {
        return getHrefLink(getTopicConsumeLink(topic, consumer), consumer);
    }

    public String getHrefLink(String link, String linkText) {
        return "<a href='" + link + "'>" + linkText + "</a>";
    }

    public String getAuditLink() {
        return getPrefix() + "admin/audit/list";
    }

    public String getClusterCapacityLink() {
        return getPrefix() + "admin/clusterCapacity";
    }

    public String getServerLink(String ip) {
        return getHrefLink(getPrefix() + "admin/server/list?ip=" + ip, ip);
    }

    public String getBrokerStoreLink(int cid, String ip) {
        return getHrefLink(getPrefix() + "admin/broker/list?cid=" + cid + "&brokerStoreIp=" + ip, ip);
    }

    public String getNameServerMonitorLink(int cid) {
        return getPrefix() + "admin/nameserver/list?cid=" + cid;
    }

    public String getControllerMonitorLink(int cid) {
        return getPrefix() + "admin/controller/list?cid=" + cid;
    }

    public String getProxyMonitorLink(int cid) {
        return getPrefix() + "admin/proxy/list?cid=" + cid;
    }

    public String getBrokerMonitorLink(int cid) {
        return getPrefix() + "admin/broker/list?cid=" + cid;
    }

    public String getBrokerAutoUpdateLink(int cid, int brokerAutoUpdateId, String brokerName) {
        return getHrefLink(getPrefix() + "admin/broker/list?cid=" + cid + "&brokerAutoUpdateId=" + brokerAutoUpdateId, brokerName);
    }

    public String getPrefix() {
        return HTTP_SCHEMA + getDomain() + "/";
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

    public Boolean getMailUseSSL() {
        return mailUseSSL;
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

    public void setMailUseSSL(Boolean mailUseSSL) {
        this.mailUseSSL = mailUseSSL;
    }

    public String getIgnoreTopic() {
        return ignoreTopic;
    }

    public Map<String, String> getClientGroupNSConfig() {
        return clientGroupNSConfig;
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

    public String getRocketmq5FilePath() {
        return rocketmq5FilePath;
    }

    public void setRocketmq5FilePath(String rocketmq5FilePath) {
        this.rocketmq5FilePath = rocketmq5FilePath;
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

    public Long getSlaveFallBehindSize() {
        return slaveFallBehindSize;
    }

    public void setSlaveFallBehindSize(Long slaveFallBehindSize) {
        this.slaveFallBehindSize = slaveFallBehindSize;
    }

    public String getMessageTypeLocation() {
        return messageTypeLocation;
    }

    public String getThreadMetricSupportedVersion() {
        return threadMetricSupportedVersion;
    }

    public void setThreadMetricSupportedVersion(String threadMetricSupportedVersion) {
        this.threadMetricSupportedVersion = threadMetricSupportedVersion;
    }

    public String getConsumeFailedMetricSupportedVersion() {
        return consumeFailedMetricSupportedVersion;
    }

    public void setConsumeFailedMetricSupportedVersion(String consumeFailedMetricSupportedVersion) {
        this.consumeFailedMetricSupportedVersion = consumeFailedMetricSupportedVersion;
    }
    
    public String getConsumeTimespanMessageSupportedVersion() {
        return consumeTimespanMessageSupportedVersion;
    }

    public void setConsumeTimespanMessageSupportedVersion(String consumeTimespanMessageSupportedVersion) {
        this.consumeTimespanMessageSupportedVersion = consumeTimespanMessageSupportedVersion;
    }

    public String getMqProxyServerString() {
        return mqProxyServerString;
    }

    public void setMqProxyServerString(String mqProxyServerString) {
        this.mqProxyServerString = mqProxyServerString;
    }

    public String getHttpProducerUriPrefix() {
        return httpProducerUriPrefix;
    }

    public void setHttpProducerUriPrefix(String httpProducerUriPrefix) {
        this.httpProducerUriPrefix = httpProducerUriPrefix;
    }

    public String getHttpConsumerUriPrefix() {
        return httpConsumerUriPrefix;
    }

    public void setHttpConsumerUriPrefix(String httpConsumerUriPrefix) {
        this.httpConsumerUriPrefix = httpConsumerUriPrefix;
    }

    public boolean isOldReqestCodeBroker(String brokerAddr) {
        if (oldReqestCodeBrokerSet == null) {
            return false;
        }
        return oldReqestCodeBrokerSet.contains(brokerAddr);
    }

    public boolean threadMetricSupported(String version) {
        return compareTo(version, threadMetricSupportedVersion) >= 0;
    }

    public boolean consumeFailedMetricSupported(String version) {
        return compareTo(version, consumeFailedMetricSupportedVersion) >= 0;
    }

    public boolean consumeTimespanMessageSupported(String version) {
        return compareTo(version, consumeTimespanMessageSupportedVersion) >= 0;
    }

    public List<String> getApiAuditUserEmail() {
        return apiAuditUserEmail;
    }

    public void setApiAuditUserEmail(List<String> apiAuditUserEmail) {
        this.apiAuditUserEmail = apiAuditUserEmail;
    }

    public int getMaxQueueNumOfFirstSearch() {
        return maxQueueNumOfFirstSearch;
    }

    public void setMaxQueueNumOfFirstSearch(int maxQueueNumOfFirstSearch) {
        this.maxQueueNumOfFirstSearch = maxQueueNumOfFirstSearch;
    }

    public String getExportedMessageRemotePath() {
        return exportedMessageRemotePath;
    }

    public void setExportedMessageRemotePath(String exportedMessageRemotePath) {
        this.exportedMessageRemotePath = exportedMessageRemotePath;
    }

    public String getExportedMessageLocalPath() {
        return exportedMessageLocalPath;
    }

    public void setExportedMessageLocalPath(String exportedMessageLocalPath) {
        this.exportedMessageLocalPath = exportedMessageLocalPath;
    }

    public String getExportedMessageDownloadUrlPrefix() {
        return exportedMessageDownloadUrlPrefix;
    }

    public void setExportedMessageDownloadUrlPrefix(String exportedMessageDownloadUrlPrefix) {
        this.exportedMessageDownloadUrlPrefix = exportedMessageDownloadUrlPrefix;
    }

    public Set<String> getIgnoreErrorProducerSet() {
        return ignoreErrorProducerSet;
    }

    public boolean isIgnoreErrorProducer(String producer) {
        if (ignoreErrorProducerSet == null) {
            return false;
        }
        return ignoreErrorProducerSet.contains(producer);
    }

    public void setIgnoreErrorProducerSet(Set<String> ignoreErrorProducerSet) {
        this.ignoreErrorProducerSet = ignoreErrorProducerSet;
    }

    public boolean checkApiAuditUserEmail(String email) {
        if (apiAuditUserEmail == null) {
            return false;
        }
        return apiAuditUserEmail.contains(email);
    }

    public Map<String, String> getRsyncConfig() {
        return rsyncConfig;
    }

    public String getRsyncUser() {
        return rsyncConfig.get("user");
    }

    public String getRsyncPassword() {
        return rsyncConfig.get("password");
    }

    public String getRsyncPort() {
        Object port = rsyncConfig.get("port");
        if (port == null) {
            return null;
        }
        return String.valueOf(port);
    }

    public String getRsyncModule() {
        return rsyncConfig.get("module");
    }

    public String getRsyncPath() {
        return rsyncConfig.get("path");
    }

    public String getRsyncBwlimit() {
        Object bwlimit = rsyncConfig.get("bwlimit");
        if (bwlimit == null) {
            return null;
        }
        return String.valueOf(bwlimit);
    }

    public String getLoginClass() {
        return loginClass;
    }

    public void setLoginClass(String loginClass) {
        this.loginClass = loginClass;
    }

    public String getMailClass() {
        return mailClass;
    }

    public void setMailClass(String mailClass) {
        this.mailClass = mailClass;
    }

    public String getUserWarnServiceClass() {
        return userWarnServiceClass;
    }

    public void setUserWarnServiceClass(String userWarnServiceClass) {
        this.userWarnServiceClass = userWarnServiceClass;
    }

    public Set<String> getConsumersForSendWarnToOut() {
        return consumersForSendWarnToOut;
    }

    public void setConsumersForSendWarnToOut(Set<String> consumersForSendWarnToOut) {
        this.consumersForSendWarnToOut = consumersForSendWarnToOut;
    }

    public boolean isConsumersForSendWarnToOut(String consumer) {
        if (consumersForSendWarnToOut == null) {
            return false;
        }
        return consumersForSendWarnToOut.contains(consumer);
    }

    public String getCommonsObjectPool2MetricsClass() {
        return commonsObjectPool2MetricsClass;
    }

    public void setCommonsObjectPool2MetricsClass(String commonsObjectPool2MetricsClass) {
        this.commonsObjectPool2MetricsClass = commonsObjectPool2MetricsClass;
    }

    /**
     * 版本号比较
     * @param v1
     * @param v2
     * @return 0:相等 1:v1>v2 -1:v1<v2
     */
    public int compareTo(String v1, String v2) {
        v1 = stripSuffix(v1);
        v2 = stripSuffix(v2);
        String[] v1Array = v1.split("\\.");
        String[] v2Array = v2.split("\\.");
        int length = Math.min(v1Array.length, v2Array.length);
        for (int i = 0; i < length; i++) {
            int v1Int = Integer.parseInt(v1Array[i]);
            int v2Int = Integer.parseInt(v2Array[i]);
            if (v1Int > v2Int) {
                return 1;
            } else if (v1Int < v2Int) {
                return -1;
            }
        }
        if (v1Array.length > v2Array.length) {
            return 1;
        } else if (v1Array.length < v2Array.length) {
            return -1;
        }
        return 0;
    }

    /**
     * 去掉版本号的后缀
     *
     * @param version
     * @return
     */
    private String stripSuffix(String version) {
        int index = version.indexOf("-");
        if (index > 0) {
            return version.substring(0, index);
        }
        return version;
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

    /**
     * broker存储耗时是否需要预警
     */
    public boolean needWarn(BrokerStoreStat brokerStoreStat) {
        Integer defaultMax = 500;
        Integer defaultPercent99 = 400;
        if (clusterStoreWarnConfig == null) {
            return brokerStoreStat.getMax() > defaultMax || brokerStoreStat.getPercent99() > defaultPercent99;
        }
        for (Map<String, Integer> map : clusterStoreWarnConfig) {
            Integer clusterId = map.get("clusterId");
            Integer max = map.get("max");
            Integer percent99 = map.get("percent99");
            // 默认值
            if (clusterId == null) {
                defaultMax = max;
                defaultPercent99 = percent99;
                continue;
            }
            // 集群匹配上采用集群配置
            if (brokerStoreStat.getClusterId() == clusterId) {
                return brokerStoreStat.getMax() > max || brokerStoreStat.getPercent99() > percent99;
            }
        }
        return brokerStoreStat.getMax() > defaultMax || brokerStoreStat.getPercent99() > defaultPercent99;
    }

    public Pair<String, String> getProxyAcl(int clusterId) {
        if (proxyAcls == null) {
            return null;
        }
        return proxyAcls.stream().filter(acls -> ((Integer) acls.get("clusterId")) == clusterId)
                .findAny()
                .map(acls -> new Pair(acls.get("accessKey"), acls.get("secretKey")))
                .orElse(null);
    }

    public String getOrderTopicKVConfig(String cid) {
        if (orderTopicKVConfig == null) {
            return null;
        }
        return orderTopicKVConfig.get(cid);
    }

    /**
     * 更新全局顺序topic kv配置
     *
     * @param cid
     * @param kvConfig
     */
    public void updateOrderTopicKVConfig(String cid, String kvConfig) {
        Result<CommonConfig> result = commonConfigService.queryByKey("orderTopicKVConfig");
        CommonConfig commonConfig = result.getResult();
        if (commonConfig == null) {
            return;
        }
        Map<String, String> orderTopicKVConfig = JSONUtil.parse(commonConfig.getValue(), Map.class);
        if (orderTopicKVConfig.containsKey(cid)) {
            return;
        }
        orderTopicKVConfig.put(cid, kvConfig);
        commonConfig.setValue(JSONUtil.toJSONString(orderTopicKVConfig));
        commonConfigService.save(commonConfig);
        try {
            init();
        } catch (Exception e) {
            logger.warn("init error:{}", e.toString());
        }
    }

    public Set<String> getMqcloudServers() {
        return mqcloudServers;
    }

    public void setMqcloudServers(Set<String> mqcloudServers) {
        this.mqcloudServers = mqcloudServers;
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
