package com.sohu.tv.mq.util;

public class CommonUtil {

    /**
     * mqcloud-test-topic -> mqcloud-test-trace-topic
     * @param topic
     * @return
     */
    public static String buildTraceTopic(String topic) {
        return topic.substring(0, topic.lastIndexOf("-")) + "-trace-topic";
    }

    public static String buildTraceTopicProducer(String traceTopic) {
        return traceTopic + "-producer";
    }
}
