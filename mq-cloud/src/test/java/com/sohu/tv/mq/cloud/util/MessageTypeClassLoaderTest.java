package com.sohu.tv.mq.cloud.util;

import org.junit.Test;

public class MessageTypeClassLoaderTest {

    @Test
    public void test() throws Exception {
        MessageTypeClassLoader messageTypeClassLoader = new MessageTypeClassLoader("classpath*:msg-type/*.class");
        System.out.println(messageTypeClassLoader.getClassNameUrlMap());
    }
    
    @Test
    public void testFile() throws Exception {
        MessageTypeClassLoader messageTypeClassLoader = new MessageTypeClassLoader("jar:file:///tmp/msgType.jar!/**/*.class");
        System.out.println(messageTypeClassLoader.getClassNameUrlMap());
    }
    
    @Test
    public void testHttp() throws Exception {
        MessageTypeClassLoader messageTypeClassLoader = new MessageTypeClassLoader("jar:http://127.0.0.1:8080/software/mqcloud/msgType.jar!/**/*.class");
        System.out.println(messageTypeClassLoader.getClassNameUrlMap());
    }
}
