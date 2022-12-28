package com.sohu.tv.mq.util;

import com.sohu.tv.mq.dto.ClusterInfoDTO;
import com.sohu.tv.mq.dto.DTOResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.utils.HttpTinyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static final String TRACE_TOPIC_SUFFIX = "-trace-topic";

    public static final String MQ_AFFINITY = "MQ_AFFINITY";

    public static final String MQ_AFFINITY_DELIMITER = "_";

    public static final String MQ_AFFINITY_DEFAULT = "default";
    
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
                logger.error("http connetion err: code:{}, info:{}", result.code, result.content);
            }
        } catch (Throwable e) {
            logger.error("http err, domain:{},topic:{},group:{},use:{}ms", mqCloudDomain, topic, group,
                    (System.currentTimeMillis() - start), e);
        }
        return null;
    }
}
