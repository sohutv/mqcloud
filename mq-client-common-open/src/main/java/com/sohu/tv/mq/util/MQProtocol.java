package com.sohu.tv.mq.util;

/**
 * MQ协议
 *
 * @Auther: yongfeigao
 * @Date: 2023/6/20
 */
public enum MQProtocol {
    ROCKETMQ(0, "rocketmq"),
    HTTP(1, "http"),
    PROXY_REMOTING(2, "proxy remoting"),
    PROXY_GRPC(3, "proxy grpc"),
    ;
    private int type;
    private String name;

    MQProtocol(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static boolean isProxyRemoting(int protocol) {
        return PROXY_REMOTING.getType() == protocol;
    }

    public static boolean isHttp(int protocol) {
        return HTTP.getType() == protocol;
    }
}
