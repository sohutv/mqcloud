package com.sohu.tv.mq.cloud.util;

/**
 * ssh异常
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月18日
 */
public class SSHException extends Exception {

    private static final long serialVersionUID = -6213665149000064880L;

    public SSHException() {
        super();
    }

    public SSHException(String message) {
        super(message);
    }

    public SSHException(String message, Throwable cause) {
        super(message, cause);
    }

    public SSHException(Throwable cause) {
        super(cause);
    }

}
