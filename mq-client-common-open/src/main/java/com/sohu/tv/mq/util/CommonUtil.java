package com.sohu.tv.mq.util;

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
}
