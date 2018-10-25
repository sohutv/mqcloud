package com.sohu.tv.mq.common;
/**
 * 降级后的异常
 * @Description: 
 * @author yongfeigao
 * @date 2018年2月12日
 */
public class FallbackException extends Exception {
    private static final long serialVersionUID = 5927923220778491308L;

    public FallbackException() {
        super();
    }

    public FallbackException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public FallbackException(String message, Throwable cause) {
        super(message, cause);
    }

    public FallbackException(String message) {
        super(message);
    }

    public FallbackException(Throwable cause) {
        super(cause);
    }
}
