package com.sohu.tv.mq.cloud.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.bo.NeedAlarmConfig;
import com.sohu.tv.mq.cloud.dao.NeedAlarmConfigDao;
import com.sohu.tv.mq.cloud.util.ConsumerWarnEnum;

/**
 * 监控获取报警配置的service
 * 
 * @author zhehongyuan
 * @date 2018年9月18日
 */
@Service
public class AlarmConfigBridingService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final long ONE_HOUR = 1 * 60 * 60 * 1000;
    
    // 用户报警配置
    private ConcurrentMap<String/* consumer */, AlarmConfig> CONFIG_TABLE = new ConcurrentHashMap<String, AlarmConfig>();

    @Autowired
    private NeedAlarmConfigDao needAlarmConfigDao;

    /**
     * 检测预警频率 参数keys必须满足 以下定义 keys——type,topic,consumer
     * 
     * @param keys
     * @return
     */
    public boolean needWarn(String... keys) {
        String key = getKey(keys);
        NeedAlarmConfig nwc = needAlarmConfigDao.get(key);
        if (nwc == null) {
            nwc = new NeedAlarmConfig();
            nwc.setoKey(key);
            nwc.setTimes(0);
            nwc.setUpdateTime(System.currentTimeMillis());
            needAlarmConfigDao.insert(nwc);
        }

        long defaultTime = getAlarmConfig(keys[2], ConsumerWarnEnum.WARN_UNIT_TIME);
        long defaultCount = getAlarmConfig(keys[2], ConsumerWarnEnum.WARN_UNIT_COUNT);
        // 均为负数说明 不接受报警
        if (defaultTime < 0 && defaultCount < 0){
            return false;
        }
        // 超过阈值，重置时间及次数
        if (System.currentTimeMillis() - nwc.getUpdateTime() > defaultTime * ONE_HOUR) {
            nwc.setTimes(0);
            needAlarmConfigDao.reset(key, System.currentTimeMillis());
        }
        // 数据库加一
        needAlarmConfigDao.updateTimes(key);
        // 控制次数
        if (nwc.getTimes() + 1 > defaultCount) {
            logger.info("key:{} times:{} not warn", key, nwc.getTimes() + 1);
            return false;
        }
        return true;
    }

    /**
     * 获取堆积数量
     * 
     * @param consumer
     * @return
     */
    public long getAccumulateCount(String consumer) {
        return getAlarmConfig(consumer, ConsumerWarnEnum.ACCUMULATE_COUNT);
    }

    /**
     * 获取堆积时间
     * 
     * @param consumer
     * @return
     */
    public long getAccumulateTime(String consumer) {
        return getAlarmConfig(consumer, ConsumerWarnEnum.ACCUMULATE_TIME);
    }

    /**
     * 获取客户端堵塞
     * 
     * @param consumer
     * @return
     */
    public long getBlockTime(String consumer) {
        return getAlarmConfig(consumer, ConsumerWarnEnum.BLOCK_TIME);
    }

    /**
     * 获取消费失败的数量
     * 
     * @param consumer
     * @return
     */
    public long getConsumerFailCount(String consumer) {
        return getAlarmConfig(consumer, ConsumerWarnEnum.CONSUMER_FAIL_COUNT);
    }

    /**
     * 获取自定义报警配置
     * 
     * @param consumer
     * @return
     */
    private Long getAlarmConfig(String consumer, ConsumerWarnEnum type) {
        AlarmConfig config = CONFIG_TABLE.get(consumer);
        if (config != null) {
            Long value = ConsumerWarnEnum.getRealValue(config, type);
            if(value != null) {
                return value;
            }
        }
        // 用户未配置预警参数，走默认值
        Long value = getDefaultConfig(type);
        if(value != null) {
            return value;
        }
        return type.getValue();
    }

    /**
     * 默认配置
     * 
     * @return
     */
    private Long getDefaultConfig(ConsumerWarnEnum type) {
        AlarmConfig defaultConfig = CONFIG_TABLE.get("");
        if (defaultConfig == null) {
            return null;
        }
        return ConsumerWarnEnum.getRealValue(defaultConfig, type);
    }

    /**
     * 将定时拉取的配置进行缓存
     * 
     * @param alarmConfigList
     */
    public void setConfigTable(List<AlarmConfig> alarmConfigList) {
        for (AlarmConfig alarmConfig : alarmConfigList) {
            CONFIG_TABLE.put(alarmConfig.getConsumer(), alarmConfig);
        }
    }

    /**
     * 拼接key
     * 
     * @param keys
     * @return
     */
    private String getKey(String... keys) {
        StringBuilder sb = new StringBuilder();
        for (String k : keys) {
            sb.append(k);
            sb.append("_");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        return "";
    }

}
