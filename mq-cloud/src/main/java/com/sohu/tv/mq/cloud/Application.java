package com.sohu.tv.mq.cloud;

import org.apache.rocketmq.client.log.ClientLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动入口
 * @Description: 
 * @author yongfeigao
 * @date 2018年1月25日
 */

@SpringBootApplication
public class Application {
    
    public static void main(String[] args) throws Exception {
        // use slf4j
        System.setProperty(ClientLogger.CLIENT_LOG_USESLF4J, "true");
        SpringApplication.run(Application.class, args);
    }

}