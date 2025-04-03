package com.sohu.tv.mq.trace;

/**
 * 记录客户端host
 */
public class ClientHostThreadLocal {
    public static final ThreadLocal<String> CLIENT_HOST_THREAD_LOCAL = new ThreadLocal<>();

    public static void set(String clientHost) {
        if (clientHost == null) {
            return;
        }
        CLIENT_HOST_THREAD_LOCAL.set(clientHost);
    }

    public static String get() {
        return CLIENT_HOST_THREAD_LOCAL.get();
    }

    public static void remove() {
        CLIENT_HOST_THREAD_LOCAL.remove();
    }
}
