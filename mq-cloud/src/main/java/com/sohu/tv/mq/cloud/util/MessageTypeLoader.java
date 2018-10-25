package com.sohu.tv.mq.cloud.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * spring boot 启动完毕loadclass
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月27日
 */
public class MessageTypeLoader extends ClassLoader {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private Map<String, URL> classUrlMap;
    
    public MessageTypeLoader(Map<String, URL> classUrlMap) {
        super();
        this.classUrlMap = classUrlMap;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        URL url = classUrlMap.get(name);
        if(url == null) {
            return super.loadClass(name);
        }
        try {
            URLConnection connection = url.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();
            while(data != -1){
                buffer.write(data);
                data = input.read();
            }
            input.close();
            byte[] classData = buffer.toByteArray();
            logger.info("load {}", name);
            return defineClass(name, classData, 0, classData.length);
        } catch (Exception e) {
            logger.error("load {}", name, e);
        }
        return null;
    }

}
