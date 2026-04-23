package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.dao.AlarmConfigDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 预警配置项
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2018年9月26日
 */
@Service
public class AlarmConfigService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AlarmConfigDao alarmConfigDao;

    /**
     * 查询所有报警配置
     * 
     * @return
     */
    public Result<List<AlarmConfig>> queryAll() {
        List<AlarmConfig> list = null;
        try {
            list = alarmConfigDao.selectAll();
        } catch (Exception e) {
            logger.error("queryAll", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 根据 consumer 查询告警配置；如果没有则回退到默认配置（consumer 为空的配置）
     */
    public Result<AlarmConfig> getAlarmConfigWithDefault(String consumer) {
        try {
            AlarmConfig defaultAlarmConfig = alarmConfigDao.selectByConsumer("");
            defaultAlarmConfig.initDefaultConfigValue();
            if (StringUtils.isEmpty(consumer)) {
                return Result.getResult(defaultAlarmConfig);
            }
            AlarmConfig alarmConfig = alarmConfigDao.selectByConsumer(consumer);
            if (alarmConfig == null) {
                alarmConfig = new AlarmConfig();
                alarmConfig.setConsumer(consumer);
            }
            alarmConfig.setDefaultConfig(defaultAlarmConfig);
            return Result.getResult(alarmConfig);
        } catch (Exception e) {
            logger.error("queryByConsumer", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 根据consumer删除报警配置
     * 
     * @return
     */
    public Result<?> deleteByConsumer(String consumer) {
        Integer count = null;
        try {
            count = alarmConfigDao.deleteByConsumer(consumer);
        } catch (Exception e) {
            logger.error("deleteByConsumer err, consumer:{}", consumer, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 更新或删除配置
     * 
     * @return
     */
    public Result<?> save(AlarmConfig alarmConfig) {
        Integer count = null;
        try {
            count = alarmConfigDao.insert(alarmConfig);
        } catch (Exception e) {
            logger.error("save err, alarmConfig:{}", alarmConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }
}
