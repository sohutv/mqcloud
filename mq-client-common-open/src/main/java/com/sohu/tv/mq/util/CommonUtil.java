package com.sohu.tv.mq.util;

import com.sohu.tv.mq.dto.ClusterInfoDTO;
import com.sohu.tv.mq.dto.DTOResult;
import com.sohu.tv.mq.dto.WebResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.utils.HttpTinyClient;
import org.apache.rocketmq.common.utils.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static final String IP = NetworkUtil.getLocalAddress();

    public static final String TRACE_TOPIC_SUFFIX = "-trace-topic";

    public static final String TRACE_TOPIC_PRODUCER_SUFFIX = "-trace-topic-producer";

    public static final String MQ_AFFINITY = "MQ_AFFINITY";

    public static final String MQ_AFFINITY_DELIMITER = "_";

    public static final String MQ_AFFINITY_DEFAULT = "default";

    private static final String CANCEL_DELAY_URL = "/topic/message/cancelWheelMsg";
    
    /**
     * mqcloud-test-topic -> mqcloud-test-trace-topic
     * @param topic
     * @return
     */
    public static String buildTraceTopic(String topic) {
        int idx = topic.lastIndexOf("-");
        if(idx == -1) {
            // 不符合规范的直接追加 TRACE_TOPIC_SUFFIX
            return topic + TRACE_TOPIC_SUFFIX;
        }
        return topic.substring(0, idx) + TRACE_TOPIC_SUFFIX;
    }

    public static String buildTraceTopicProducer(String traceTopic) {
        return traceTopic + "-producer";
    }
    
    /**
     * 是否是trace topic
     * @param topic
     * @return
     */
    public static boolean isTraceTopic(String topic) {
        if(topic == null) {
            return false;
        }
        return topic.endsWith(TRACE_TOPIC_SUFFIX);
    }

    /**
     * 是否是trace topic producer
     *
     * @param producer
     * @return
     */
    public static boolean isTraceTopicProducer(String producer) {
        if (producer == null) {
            return false;
        }
        return producer.endsWith(TRACE_TOPIC_PRODUCER_SUFFIX);
    }
    
    /*
     * 是否是重试topic
     */
    public static boolean isRetryTopic(String topic) {
        if(topic == null) {
            return false;
        }
        return topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX);
    }
    
    /**
     * 是否是死队列
     * @return
     */
    public static boolean isDeadTopic(String topic) {
        if(topic == null) {
            return false;
        }
        return topic.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX);
    }

    /**
     * 获取生产或消费的集群配置
     * @param mqCloudDomain
     * @param topic
     * @param group
     * @param role
     * @return
     */
    public static ClusterInfoDTO fetchClusterInfo(String mqCloudDomain, String topic, String group, int role) {
        long start = System.currentTimeMillis();
        List<String> paramValues = new ArrayList<String>();
        paramValues.add("topic");
        paramValues.add(topic);
        paramValues.add("group");
        paramValues.add(group);
        paramValues.add("role");
        paramValues.add(String.valueOf(role));
        paramValues.add("v");
        paramValues.add(Version.get());
        // 从MQCLoud拉取配置信息
        HttpTinyClient.HttpResult result = null;
        try {
            result = HttpTinyClient.httpGet("http://" + mqCloudDomain + "/cluster/info", null,
                    paramValues,
                    "UTF-8", 3000);
            if (HttpURLConnection.HTTP_OK == result.code) {
                DTOResult<ClusterInfoDTO> clusterInfoDTOResult = JSONUtil.parse(result.content, DTOResult.class,
                        ClusterInfoDTO.class);
                if (clusterInfoDTOResult == null) {
                    return null;
                }
                if (clusterInfoDTOResult.ok()) {
                    return clusterInfoDTOResult.getResult();
                } else {
                    if (clusterInfoDTOResult.getStatus() == 201) {
                        logger.warn("please register your {}:{} topic:{} in MQCloud!", role == 1 ? "producer"
                                : "consumer", group, topic);
                    } else {
                        logger.warn("fetch topic:{} group:{} cluster info err:{}", topic, group,
                                clusterInfoDTOResult.getMessage());
                    }
                }
            } else {
                logger.error("http connection err: code:{}, info:{}", result.code, result.content);
            }
        } catch (Throwable e) {
            logger.error("http err, domain:{},topic:{},group:{},use:{}ms", mqCloudDomain, topic, group,
                    (System.currentTimeMillis() - start), e);
        }
        return null;
    }

    public static WebResult<String> cancelDelayedMsg(String topic, String uniqId,
                                                         String token, String domain) {
        List<String> paramValues = new ArrayList<String>();
        // topic
        paramValues.add("topic");
        paramValues.add(topic);
        // uniqId
        paramValues.add("uniqIds");
        paramValues.add(uniqId);
        // headers
        List<String> headers = new ArrayList<String>();
        headers.add("Cookie");
        headers.add("TOKEN=" + token);
        // real post
        try {
            HttpTinyClient.HttpResult result = HttpTinyClient.httpPost("http://" + domain + CANCEL_DELAY_URL,
                    headers, paramValues, "UTF-8", 5000);
            if (HttpURLConnection.HTTP_OK == result.code) {
                WebResult sendResult = JSONUtil.parse(result.content, WebResult.class,
                        String.class);
                return sendResult;
            } else {
                return WebResult.setFail(result.code, result.content);
            }
        } catch (Exception e) {
            logger.error("cancelDelayedMsg err, topic:{}, uniqId:{}", topic, uniqId, e);
            return WebResult.setFail(500, e.getMessage());
        }
    }
}
