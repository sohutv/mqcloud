package com.sohu.tv.mq.util;

import com.sohu.index.tv.mq.common.Result;
import com.sohu.tv.mq.dto.ClusterInfoDTO;
import com.sohu.tv.mq.dto.DTOResult;
import com.sohu.tv.mq.dto.WebResult;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.utils.HttpTinyClient;
import org.apache.rocketmq.common.utils.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    public static final String IP = NetworkUtil.getLocalAddress();

    public static final String TRACE_TOPIC_SUFFIX = "-trace-topic";

    public static final String TRACE_TOPIC_PRODUCER_SUFFIX = "-trace-topic-producer";

    public static final String MQ_AFFINITY = "MQ_AFFINITY";

    public static final String MQ_AFFINITY_DELIMITER = "_";

    public static final String MQ_AFFINITY_DEFAULT = "default";

    private static final String CANCEL_DELAY_URL = "/topic/message/cancelWheelMsg";

    public static final String LMQ_TOPIC_SEPARATOR = "$";
    
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
    public static Result<ClusterInfoDTO> fetchClusterInfo(String mqCloudDomain, Map<String, String> params) {
        long start = System.currentTimeMillis();
        List<String> paramValues = new ArrayList<>();
        for (Entry<String, String> entry : params.entrySet()) {
            paramValues.add(entry.getKey());
            paramValues.add(entry.getValue());
        }
        Result<ClusterInfoDTO> result = new Result<>(true);
        // 从MQCLoud拉取配置信息
        try {
            HttpTinyClient.HttpResult response = HttpTinyClient.httpGet("http://" + mqCloudDomain + "/cluster/info", null,
                    paramValues,
                    "UTF-8", 3000);
            if (HttpURLConnection.HTTP_OK == response.code) {
                DTOResult<ClusterInfoDTO> clusterInfoDTOResult = JSONUtil.parse(response.content, DTOResult.class,
                        ClusterInfoDTO.class);
                if (clusterInfoDTOResult == null) {
                    return result;
                }
                if (clusterInfoDTOResult.ok()) {
                    result.setResult(clusterInfoDTOResult.getResult());
                    return result;
                } else {
                    if (clusterInfoDTOResult.getStatus() == 201) {
                        logger.warn("please register in MQCloud!, params:{}", params);
                    } else {
                        logger.warn("fetch cluster info err:{}, params:{}", clusterInfoDTOResult.getMessage(), params);
                    }
                }
            } else {
                result.setSuccess(false);
                logger.error("http connection err: code:{}, info:{}", response.code, response.content);
            }
        } catch (Throwable e) {
            logger.error("domain:{} use:{}ms, params:{}", mqCloudDomain, (System.currentTimeMillis() - start), params, e);
            result.setSuccess(false);
            result.setException(e);
        }
        return result;
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

    public static String stripLmqPrefix(String consumer) {
        return consumer.substring(MixAll.LMQ_PREFIX.length());
    }

    public static String stripLmqParentPrefix(String topic) {
        return topic.substring(topic.indexOf(LMQ_TOPIC_SEPARATOR) + 1);
    }

    public static String findLmqParentTopic(String topic) {
        String tmpTopic = stripLmqPrefix(topic);
        return tmpTopic.substring(0, tmpTopic.indexOf(LMQ_TOPIC_SEPARATOR));
    }

    public static String buildLmqTopic(String parentTopic, String lmqTopic) {
        return MixAll.LMQ_PREFIX + parentTopic + LMQ_TOPIC_SEPARATOR + lmqTopic;
    }

    public static String buildLmqParentTopic(String parentTopic) {
        return MixAll.LMQ_PREFIX + parentTopic;
    }

    public static String buildLmqConsumer(String consumer) {
        return MixAll.LMQ_PREFIX + consumer;
    }

    public static long getLmqOffset(String liteTopic, MessageExt msg) {
        if (!MixAll.isLmq(liteTopic)) {
            return msg.getQueueOffset();
        }
        String[] queues = msg.getProperty(MessageConst.PROPERTY_INNER_MULTI_DISPATCH).split(MixAll.LMQ_DISPATCH_SEPARATOR);
        String[] queueOffsets = msg.getProperty(MessageConst.PROPERTY_INNER_MULTI_QUEUE_OFFSET).split(MixAll.LMQ_DISPATCH_SEPARATOR);
        return Long.parseLong(queueOffsets[ArrayUtils.indexOf(queues, liteTopic)]);
    }

    public static String getLmqOffsetInfo(MessageExt msg) {
        String liteTopic = msg.getProperty(MessageConst.PROPERTY_INNER_MULTI_DISPATCH);
        String liteTopicQueueOffset = msg.getProperty(MessageConst.PROPERTY_INNER_MULTI_QUEUE_OFFSET);
        if (liteTopic == null || liteTopicQueueOffset == null) {
            return null;
        }
        String[] liteTopics = liteTopic.split(MixAll.LMQ_DISPATCH_SEPARATOR);
        String[] liteTopicQueueOffsets = liteTopicQueueOffset.split(MixAll.LMQ_DISPATCH_SEPARATOR);
        if (liteTopics.length != liteTopicQueueOffsets.length) {
            return null;
        }
        StringBuilder lmqOffsetInfo = new StringBuilder();
        for (int i = 0; i < liteTopics.length; i++) {
            if (i > 0) {
                lmqOffsetInfo.append(";");
            }
            // strip MixAll.LMQ_PREFIX
            String topic = CommonUtil.stripLmqParentPrefix(liteTopics[i]);
            lmqOffsetInfo.append(topic).append(":").append(liteTopicQueueOffsets[i]);
        }
        return lmqOffsetInfo.toString();
    }
}
