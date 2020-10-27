package com.sohu.tv.mq.common;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.log.ClientLogger;
import org.apache.rocketmq.client.trace.AsyncTraceDispatcher;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.utils.HttpTinyClient;
import org.apache.rocketmq.common.utils.HttpTinyClient.HttpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sohu.tv.mq.dto.ClusterInfoDTO;
import com.sohu.tv.mq.dto.DTOResult;
import com.sohu.tv.mq.serializable.MessageSerializer;
import com.sohu.tv.mq.serializable.MessageSerializerEnum;
import com.sohu.tv.mq.trace.SohuAsyncTraceDispatcher;
import com.sohu.tv.mq.trace.TraceRocketMQProducer;
import com.sohu.tv.mq.util.CommonUtil;
import com.sohu.tv.mq.util.Constant;
import com.sohu.tv.mq.util.Version;

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

    public AbstractConfig(String group, String topic) {
        this.topic = topic;
        this.group = group;
        // use slf4j
        System.setProperty(ClientLogger.CLIENT_LOG_USESLF4J, "true");
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroup() {
        return group;
    }

    /**
     * 初始化
     */
    protected void init() {
        List<String> paramValues = new ArrayList<String>();
        paramValues.add("topic");
        paramValues.add(getTopic());
        paramValues.add("group");
        paramValues.add(group);
        paramValues.add("role");
        paramValues.add(String.valueOf(role()));
        paramValues.add("v");
        paramValues.add(Version.get());
        // 从MQCLoud拉取配置信息
        long times = 1;
        while (true) {
            DTOResult<ClusterInfoDTO> clusterInfoDTOResult = null;
            try {
                HttpResult result = HttpTinyClient.httpGet("http://" + mqCloudDomain + "/cluster/info", null,
                        paramValues,
                        "UTF-8", 3000);
                if (HttpURLConnection.HTTP_OK == result.code) {
                    clusterInfoDTOResult = JSON.parseObject(result.content, new TypeReference<DTOResult<ClusterInfoDTO>>(){});
                    if (clusterInfoDTOResult.ok()) {
                        clusterInfoDTO = clusterInfoDTOResult.getResult();
                    }
                } else {
                    logger.error("http connetion err: code:{},info:{}", result.code, result.content);
                }
            } catch (Throwable e) {
                logger.error("http err, topic:{},group:{}", topic, group, e);
            }
            if (clusterInfoDTO == null) {
                if (clusterInfoDTOResult.getStatus() == 201) {
                    logger.warn("please register your {}:{} topic:{} in MQCloud first, times:{}",
                            role() == 1 ? "producer" : "consumer", group, topic, times++);
                } else {
                    logger.warn("fetch cluster info err:{}, times:{}", clusterInfoDTOResult.getMessage(), times++);
                }
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
        // 设置instanceName
        if(getInstanceName() != null) {
            clientConfig.setInstanceName(getInstanceName());
        }
        // 设置序列化方式
        messageSerializer = MessageSerializerEnum.getMessageSerializerByType(
                clusterInfoDTO.getSerializer());
        if(messageSerializer == null) {
            logger.error("serializer is null! clusterInfoDTO:{}", clusterInfoDTO.toString());
        }
        // 客户端ip初始化
        initClientIp(clientConfig);
        // trace 初始化
        initTrace();
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
            // 初始化TraceDispatcher
            SohuAsyncTraceDispatcher traceDispatcher = new SohuAsyncTraceDispatcher(traceTopic);
            // 设置producer属性
            traceRocketMQProducer.getProducer().setSendMsgTimeout(5000);
            traceRocketMQProducer.getProducer().setMaxMessageSize(traceDispatcher.getMaxMsgSize() - 10 * 1000);
            traceRocketMQProducer.setMqCloudDomain(mqCloudDomain);
            traceRocketMQProducer.setInstanceName(String.valueOf(role()));
            // 启动trace producer
            traceRocketMQProducer.start();
            // 赋给TraceDispatcher
            traceDispatcher.setTraceProducer(traceRocketMQProducer.getProducer());
            // 启动
            traceDispatcher.start();
            // 注册
            registerTraceDispatcher(traceDispatcher);
        } catch (Exception e) {
            logger.error("SohuAsyncTraceDispatcher init err", e);
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
     * 当存在同一JVM内需要向多个RocketMQ集群生产消息时, 需要设置InstanceName作区分，否则默认发往第一个初始化的集群。
     * 注：之所以追加pid是因为如果集群消费方式的consumer设置了相同的instanceName，会导致消费不均
     * 参考：https://blog.csdn.net/a417930422/article/details/50663629
     * 
     * @param instanceName
     */
    public void setInstanceName(String instanceName) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(UtilAll.getPid());
        buffer.append("@");
        buffer.append(instanceName);
        this.instanceName = buffer.toString();
    }

    public String getInstanceName() {
        return instanceName;
    }
}
