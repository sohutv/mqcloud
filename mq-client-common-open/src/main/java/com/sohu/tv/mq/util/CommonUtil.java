package com.sohu.tv.mq.util;

import org.apache.rocketmq.common.MixAll;

public class CommonUtil {

    public static final String TRACE_TOPIC_SUFFIX = "-trace-topic";
    
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
        return topic.endsWith(TRACE_TOPIC_SUFFIX);
    }
    
    /*
     * 是否是重试topic
     */
    public static boolean isRetryTopic(String topic) {
        return topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX);
    }
    
    /**
     * 是否是死队列
     * @return
     */
    public static boolean isDeadTopic(String topic) {
        return topic.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX);
    }
}
