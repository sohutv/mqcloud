package com.sohu.tv.mq.cloud.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.service.AlarmConfigBridingService;
import com.sohu.tv.mq.cloud.service.AlarmConfigService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 报警配置定时刷新
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月30日
 */
public class AlarmConfigTask {
    private static final Logger logger = LoggerFactory.getLogger(AlarmConfigTask.class);

    @Autowired
    private AlarmConfigBridingService alarmConfigBridingService;

    @Autowired
    private AlarmConfigService alarmConfigService;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Scheduled(cron = "3 */10 * * * *")
    public void refreshAlarmConfig() {
        long start = System.currentTimeMillis();
        Result<List<AlarmConfig>> userAlarmConfigResult = alarmConfigService.queryAll();
        if (userAlarmConfigResult.isNotOK()) {
            logger.error("refresh user alarm config err:{}", userAlarmConfigResult);
            return;
        }
        alarmConfigBridingService.setConfigTable(userAlarmConfigResult.getResult());
        // 更新mqcloud配置
        try {
            mqCloudConfigHelper.init();
        } catch (Exception e) {
            logger.error("");
        }
        logger.info("refresh alarm config use: {}ms", (System.currentTimeMillis() - start));
    }
}
