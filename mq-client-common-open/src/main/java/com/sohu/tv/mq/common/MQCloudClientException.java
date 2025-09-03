package com.sohu.tv.mq.common;

/**
 * MQCloud客户端异常
 *
 * @author yongfeigao
 * @date 2025年08月12日
 */
public class MQCloudClientException extends RuntimeException {
    public MQCloudClientException() {
        super();
    }

    public MQCloudClientException(String message) {
        super(message);
    }

    public MQCloudClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
