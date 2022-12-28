package com.sohu.tv.mq.common;

import com.sohu.tv.mq.dto.ClusterInfoDTO;
import com.sohu.tv.mq.serializable.MessageSerializer;
import com.sohu.tv.mq.serializable.MessageSerializerEnum;
import com.sohu.tv.mq.trace.SohuAsyncTraceDispatcher;
import com.sohu.tv.mq.trace.TraceRocketMQProducer;
import com.sohu.tv.mq.util.CommonUtil;
import com.sohu.tv.mq.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 公共配置
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年5月15日
 */
public abstract class AbstractConfig {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final int PRODUCER = 1;

    protected final int CONSUMER = 2;

    /**
     * 主题
     */
    protected String topic;

    /**
     * 组
     */
    protected String group;

    // 配置信息
    private ClusterInfoDTO clusterInfoDTO;

    // 是否开启数据采样 默认开启
    private boolean sampleEnabled = true;

    // mqcloud的域名
    private String mqCloudDomain;
    
    // 消息序列化工具
    private MessageSerializer<Object> messageSerializer;

    // 是否开启trace
    protected boolean traceEnabled;

    // 是否设置了instanceName
    protected String instanceName;
    
    protected SohuAsyncTraceDispatcher traceDispatcher;

    // 启用亲和性
    private boolean affinityEnabled;

    // 亲和的broker，broker名与机房名采用下划线分割，比如broker-a_bx，表示bx机房的broker-a
    private String affinityBrokerSuffix;

    public AbstractConfig() {
        this(null, null);
    }

    public AbstractConfig(String group, String topic) {
        this.topic = topic;
        this.group = group;
        // use slf4j
        System.setProperty(ClientLogger.CLIENT_LOG_USESLF4J, "true");
        // 设置亲和性
        setAffinity();
    }

    public void setAffinity(){
        // 优先使用jvm属性
        setAffinityBrokerSuffix(System.getProperty(CommonUtil.MQ_AFFINITY));
        if (getAffinityBrokerSuffix() == null) {
            // 其次使用系统变量
            setAffinityBrokerSuffix(System.getenv(CommonUtil.MQ_AFFINITY));
        }
        // 如果外部配置了亲和性，则开启
        if (getAffinityBrokerSuffix() != null) {
            setAffinityEnabled(true);
        }
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    /**
     * 初始化
     */
    protected void init() {
        while (true) {
            clusterInfoDTO = CommonUtil.fetchClusterInfo(mqCloudDomain, getTopic(), group, role());
            if (clusterInfoDTO == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.warn("init interrupted");
                }
            } else {
                logger.info("topic:{}, group:{}, role:{}, init ok:{}", getTopic(), group, role(), clusterInfoDTO);
                break;
            }
        }
        setProperty(Constant.ROCKETMQ_NAMESRV_DOMAIN, getMqCloudDomain());
    }

    /**
     * 设置系统属性
     * 
     * @param properties
     * @param key
     */
    protected void setProperty(String key, String value) {
        String prev = System.getProperty(key);
        if (prev == null) {
            System.setProperty(key, value);
            logger.info("group:{} topic:{},set property {}={}", group, topic, key, value);
        } else {
            logger.info("group:{} topic:{},cannot set property {}={}, prev={} exist!!", group, topic, key, value, prev);
        }
    }

    public ClusterInfoDTO getClusterInfoDTO() {
        return clusterInfoDTO;
    }

    /**
     * 初始化属性
     * 
     * @param clientConfig
     */
    protected void initConfig(ClientConfig clientConfig) {
        init();
        // 客户端主动设置为false，不覆盖
        if(clientConfig.isVipChannelEnabled()) {
            // 低版本集群不支持vip通道
            clientConfig.setVipChannelEnabled(clusterInfoDTO.isVipChannelEnabled());
        }
        // 通过unitName发现不同的集群
        clientConfig.setUnitName(String.valueOf(clusterInfoDTO.getClusterId()));
        // 自动设置是否trace
        traceEnabled = clusterInfoDTO.isTraceEnabled();
        // 设置序列化方式
        messageSerializer = MessageSerializerEnum.getMessageSerializerByType(
                clusterInfoDTO.getSerializer());
        if(messageSerializer == null) {
            logger.error("serializer is null! clusterInfoDTO:{}", clusterInfoDTO.toString());
        }
        // 客户端ip初始化
        initClientIp(clientConfig);
        // 亲和性初始化
        initAffinity();
        // 构建instanceName
        buildInstanceName();
        // trace 初始化
        initTrace();
        // 设置instanceName
        if(getInstanceName() != null) {
            clientConfig.setInstanceName(getInstanceName());
        }
    }
    
    /**
     * 为了防止服务器多网卡或docker情况无法获取正确ip
     * @param clientConfig
     */
    protected void initClientIp(ClientConfig clientConfig) {
        String ip = System.getProperty("MY_POD_IP");
        if (StringUtils.isEmpty(ip)) {
            ip = System.getenv("MY_POD_IP");
        }
        if (StringUtils.isEmpty(ip)) {
            return;
        }
        ip = ip.trim();
        for (String address : MixAll.getLocalInetAddress()) {
            if (ip.equals(address)) {
                logger.info("topic:{} group:{} useIp:{}", getTopic(), getGroup(), ip);
                clientConfig.setClientIP(ip);
                return;
            }
        }
        logger.warn("MY_POD_IP:{} not in {}", ip, MixAll.getLocalInetAddress());
    }

    /**
     * 初始化trace;
     */
    protected void initTrace() {
        if (!isTraceEnabled()) {
            return;
        }
        try {
            // 构建trace专用topic
            String traceTopic = CommonUtil.buildTraceTopic(topic);
            // 构建单独的trace producer
            TraceRocketMQProducer traceRocketMQProducer = new TraceRocketMQProducer(
                    CommonUtil.buildTraceTopicProducer(traceTopic), traceTopic);
            // 设置producer属性
            traceRocketMQProducer.getProducer().setSendMsgTimeout(5000);
            traceRocketMQProducer.setMqCloudDomain(mqCloudDomain);
            traceRocketMQProducer.setInstanceName(getGroup());
            // 采用外部生产者或消费者的亲和设置
            traceRocketMQProducer.setAffinityEnabled(affinityEnabled);
            traceRocketMQProducer.setAffinityBrokerSuffix(affinityBrokerSuffix);
            // 启动trace producer
            traceRocketMQProducer.start();
            // 初始化TraceDispatcher
            traceDispatcher = new SohuAsyncTraceDispatcher(traceTopic, traceRocketMQProducer.getProducer());
            // 启动
            traceDispatcher.start(null, null);
            // 注册
            registerTraceDispatcher(traceDispatcher);
        } catch (Exception e) {
            logger.error("SohuAsyncTraceDispatcher init err", e);
        }
    }

    /**
     * 亲和性初始化
     */
    protected void initAffinity() {
        if (!affinityEnabled) {
            return;
        }
        if (affinityBrokerSuffix == null) {
            throw new IllegalArgumentException("affinityBrokerSuffix cannot be null");
        }
    }

    /**
     * 角色
     * 
     * @return
     */
    protected abstract int role();

    /**
     * 注册trace hook
     * 
     * @param traceDispatcher
     */
    protected void registerTraceDispatcher(AsyncTraceDispatcher traceDispatcher) {
        throw new UnsupportedOperationException("not impl!");
    }

    public boolean isSampleEnabled() {
        return sampleEnabled;
    }

    public void setSampleEnabled(boolean sampleEnabled) {
        this.sampleEnabled = sampleEnabled;
    }

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    public String getMqCloudDomain() {
        return mqCloudDomain;
    }

    public void setMqCloudDomain(String mqCloudDomain) {
        this.mqCloudDomain = mqCloudDomain;
    }

    public MessageSerializer<Object> getMessageSerializer() {
        return messageSerializer;
    }

    public void setMessageSerializer(MessageSerializer<Object> messageSerializer) {
        this.messageSerializer = messageSerializer;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * 设置instanceName
     *
     * @param instanceName
     */
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    /**
     * 构建instanceName
     */
    public void buildInstanceName() {
        if (affinityEnabled) {
            if (this.instanceName == null) {
                this.instanceName = UtilAll.getPid() + CommonUtil.MQ_AFFINITY_DELIMITER + affinityBrokerSuffix;
            } else {
                this.instanceName = UtilAll.getPid() + CommonUtil.MQ_AFFINITY_DELIMITER + affinityBrokerSuffix + "@" + this.instanceName;
            }
        } else {
            if (this.instanceName != null) {
                this.instanceName = UtilAll.getPid() + "@" + this.instanceName;
            }
        }
    }

    public String getInstanceName() {
        return instanceName;
    }
    
    public void shutdown(){
    	if(traceDispatcher != null){
    		traceDispatcher.shutdown();
    	}
    }

    public boolean isAffinityEnabled() {
        return affinityEnabled;
    }

    public void setAffinityEnabled(boolean affinityEnabled) {
        this.affinityEnabled = affinityEnabled;
    }

    public String getAffinityBrokerSuffix() {
        return affinityBrokerSuffix;
    }

    public void setAffinityBrokerSuffix(String affinityBrokerSuffix) {
        this.affinityBrokerSuffix = affinityBrokerSuffix;
    }

    /**
     * 如果broker设置亲和标记，当客户端标记为default时，亲和该broker
     *
     * @return
     */
    public boolean isAffinityIfBrokerNotSet() {
        return CommonUtil.MQ_AFFINITY_DEFAULT.equals(affinityBrokerSuffix);
    }
}
