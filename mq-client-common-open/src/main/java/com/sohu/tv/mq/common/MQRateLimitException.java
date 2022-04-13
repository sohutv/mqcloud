package com.sohu.tv.mq.common;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.remoting.protocol.RemotingSysResponseCode;

/**
 * @author: yongfeigao
 * @date: 2022/3/2 15:46
 */
public class MQRateLimitException extends MQBrokerException {
    // 静态引用
    public static final MQRateLimitException MQ_RATELIMIT_EXCEPTION = new MQRateLimitException();

    public MQRateLimitException() {
        super(RemotingSysResponseCode.SYSTEM_BUSY, "RateLimit");
    }
    public MQRateLimitException(int responseCode, String errorMessage) {
        super(responseCode, errorMessage);
    }
    public MQRateLimitException(int responseCode, String errorMessage, String brokerAddr) {
        super(responseCode, errorMessage, brokerAddr);
    }

    /**
     * 尝试转换为MQRateLimitException
     * @param exception
     * @return
     */
    public static Throwable tryToChange(Throwable exception) {
        if (isRateLimited(exception)) {
            return MQ_RATELIMIT_EXCEPTION;
        }
        return exception;
    }

    /**
     * 是否被限速了
     * @param exception
     * @return
     */
    public static boolean isRateLimited(Throwable exception) {
        if (exception != null && exception instanceof MQBrokerException) {
            MQBrokerException mqBrokerException = (MQBrokerException) exception;
            if (mqBrokerException.getResponseCode() == RemotingSysResponseCode.SYSTEM_BUSY
                    && mqBrokerException.getErrorMessage() != null
                    && mqBrokerException.getErrorMessage().contains("RateLimit")) {
                return true;
            }
        }
        return false;
    }
}
