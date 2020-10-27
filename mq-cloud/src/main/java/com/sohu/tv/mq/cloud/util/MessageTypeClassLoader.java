package com.sohu.tv.mq.cloud.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * 消息类型class加载
 * 
 * @author yongfeigao
 * @date 2020年10月10日
 */
public class MessageTypeClassLoader extends ClassLoader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 消息类型位置
    private final String messageTypeLocation;

    // 存储className->URL
    private Map<String, URL> classNameUrlMap;

    /**
     * 构造
     * 
     * @param locationPattern 支持 @PathMatchingResourcePatternResolver
     * @throws Exception
     */
    public MessageTypeClassLoader(String locationPattern) throws Exception {
        this.messageTypeLocation = locationPattern;
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resourcePatternResolver.getResources(locationPattern);
        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        classNameUrlMap = new HashMap<String, URL>();
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                classNameUrlMap.put(className, resource.getURL());
            }
        }
        logger.info("init {} with classNameUrlMap:{}", locationPattern, classNameUrlMap);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        URL url = classNameUrlMap.get(name);
        if (url == null) {
            return super.loadClass(name);
        }
        try {
            URLConnection connection = url.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();
            while (data != -1) {
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

    public Map<String, URL> getClassNameUrlMap() {
        return classNameUrlMap;
    }

    public String getMessageTypeLocation() {
        return messageTypeLocation;
    }
}
