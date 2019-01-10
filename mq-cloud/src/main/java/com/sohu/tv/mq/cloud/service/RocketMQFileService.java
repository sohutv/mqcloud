package com.sohu.tv.mq.cloud.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;

/**
 * rocketmq.zip文件辅助服务
 * 
 * @author zhehongyuan
 * @date 2018年1月7号
 */
@Service
public class RocketMQFileService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    public byte[] getRocketmqFile() {
        InputStream in = null;
        String path = mqCloudConfigHelper.getRocketmqFilePath().trim();
        ResourceLoader loader = new DefaultResourceLoader();
        try {
            in = loader.getResource(path).getInputStream();
        } catch (IOException e) {
            logger.error("load RocketMQ Installation Package err! path:{}", path);
            return null;
        }
        return getFileContent(in);
    }

    /**
     * 获取文件
     * @param in
     * @return
     */
    public byte[] getFileContent(InputStream in) {
        ByteArrayOutputStream out = null;
        byte[] buffer = new byte[1024 * 4];
        try {
            out = new ByteArrayOutputStream();
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        } catch (IOException e) {
            logger.error("read rocketmq.zip err!");
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            buffer = null;
        }
    }
}
