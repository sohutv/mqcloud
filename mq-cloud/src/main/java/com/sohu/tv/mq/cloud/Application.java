package com.sohu.tv.mq.cloud;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动入口
 * @Description: 
 * @author yongfeigao
 * @date 2018年1月25日
 */
@EnableScheduling
@SpringBootApplication
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
public class Application {
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

}