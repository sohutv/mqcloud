package com.sohu.tv.mq.util;

public class CommonUtil {

    public static final String TRACE_TOPIC_SUFFIX = "-trace-topic";
    
    /**
     * mqcloud-test-topic -> mqcloud-test-trace-topic
     * @param topic
     * @return
     */
    public static String buildTraceTopic(String topic) {
        return topic.substring(0, topic.lastIndexOf("-")) + TRACE_TOPIC_SUFFIX;
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
