package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author yongweizhao
 * @create 2020/8/5 17:04
 */
@Component
@Order(10)
public class TrafficStatServiceWarmup implements CommandLineRunner {

    @Autowired
    private TopicTrafficStatService topicTrafficStatService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Override
    public void run(String... strings) throws Exception {
        if (mqCloudConfigHelper.isOnline()) {
            topicTrafficStatService.trafficStatAll();
        }
    }
}
