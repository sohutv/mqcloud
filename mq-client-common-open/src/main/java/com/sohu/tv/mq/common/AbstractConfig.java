package com.sohu.tv.mq.common;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.common.utils.HttpTinyClient;
import org.apache.rocketmq.common.utils.HttpTinyClient.HttpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.dto.ClusterInfoDTO;
import com.sohu.tv.mq.dto.ClusterInfoDTOResult;
import com.sohu.tv.mq.serializable.MessageSerializer;
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

    public AbstractConfig(String group, String topic) {
        this.topic = topic;
        this.group = group;
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
            ClusterInfoDTOResult clusterInfoDTOResult = null;
            try {
                HttpResult result = HttpTinyClient.httpGet("http://" + mqCloudDomain + "/cluster/info", null,
                        paramValues,
                        "UTF-8", 3000);
                if (HttpURLConnection.HTTP_OK == result.code) {
                    clusterInfoDTOResult = JSON.parseObject(result.content, ClusterInfoDTOResult.class);
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
        // 低版本集群不支持vip通道
        clientConfig.setVipChannelEnabled(clusterInfoDTO.isVipChannelEnabled());
        // 通过unitName发现不同的集群
        clientConfig.setUnitName(String.valueOf(clusterInfoDTO.getClusterId()));
    }

    /**
     * 角色
     * 
     * @return
     */
    protected abstract int role();

    public boolean isSampleEnabled() {
        return sampleEnabled;
    }

    public void setSampleEnabled(boolean sampleEnabled) {
        this.sampleEnabled = sampleEnabled;
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
}
