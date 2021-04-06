package com.sohu.tv.mq.common;

/**
 * 消费异常
 * 
 * @author yongfeigao
 * @date 2020年12月21日
 */
public class ConsumeException extends Exception {
    private static final long serialVersionUID = 3662764764579878687L;

    public ConsumeException() {
        super();
    }

    public ConsumeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConsumeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsumeException(String message) {
        super(message);
    }

    public ConsumeException(Throwable cause) {
        super(cause);
    }
}
