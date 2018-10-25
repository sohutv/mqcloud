package com.sohu.tv.mq.cloud.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sohu.tv.mq.cloud.bo.NeedAlarmConfig;
import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.dao.AlarmConfigDao;
import com.sohu.tv.mq.cloud.dao.NeedAlarmConfigDao;

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

    private static final int COUNT = 2;

    // 用户报警配置
    private ConcurrentMap<String/* topic_uid */, AlarmConfig> USER_CONFIG_TABLE = new ConcurrentHashMap<String, AlarmConfig>();

    @Autowired
    private AlarmConfigDao alarmConfigDao;

    @Autowired
    private NeedAlarmConfigDao needAlarmConfigDao;

    private AlarmConfig defaultConfig;

    /**
     * 是否应该报警 此处暂不考虑用户的配置，全部走默认配置
     * 
     * @param key
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

        long defaultTime = getDefaultConfig() == null ? ONE_HOUR
                : (getDefaultConfig().getWarnUnitTime() * ONE_HOUR);
        // 超过阈值，重置时间及次数
        if (System.currentTimeMillis() - nwc.getUpdateTime() > defaultTime) {
            nwc.setTimes(0);
            needAlarmConfigDao.reset(key, System.currentTimeMillis());
        }
        int defaultCount = getDefaultConfig() == null ? COUNT : getDefaultConfig().getWarnUnitCount();
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
     * 获取消费失败的数量
     * 
     * @param uid
     * @param topic
     * @return
     */
    public long getConsumerFailCount(long uid, String topic) {
        AlarmConfig alarmConfig = getAlarmConfig(uid, topic);
        if (null == alarmConfig) {
            return (long) 10;
        }
        // 不接受报警返回负数
        if (!alarmConfig.isAlert()) {
            return -1;
        }
        return alarmConfig.getConsumerFailCount();
    }

    /**
     * 获取用户报警配置
     * 
     * @param uid
     * @param topic
     * @return
     */
    public AlarmConfig getAlarmConfig(long uid, String topic) {
        String key = topic + "_" + uid;
        AlarmConfig userAlarmConfig = USER_CONFIG_TABLE.get(key);
        if (null != userAlarmConfig) {
            return userAlarmConfig;
        }
        AlarmConfig defaultAlarmConfig = getDefaultConfig();
        return defaultAlarmConfig;
    }

    /**
     * 默认配置
     * 
     * @return
     */
    public AlarmConfig getDefaultConfig() {
        if (null == defaultConfig) {
            List<AlarmConfig> defaultAlarmConfig = alarmConfigDao.selectByUid(0);
            if (null != defaultAlarmConfig && defaultAlarmConfig.size() > 0) {
                setDefaultConfig(defaultAlarmConfig.get(0));
                return defaultAlarmConfig.get(0);
            } else {
                logger.error("get no result form warn_config, default alarm config not exist");
            }
        }
        return defaultConfig;
    }

    public void setDefaultConfig(AlarmConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    /**
     * 将定时拉取的用户配置进行缓存
     * 
     * @param alarmConfigList
     */
    public void setUserConfigTable(List<AlarmConfig> alarmConfigList) {
        for (AlarmConfig alarmConfig : alarmConfigList) {
            String key = alarmConfig.getTopic() + "_" + alarmConfig.getUid();
            USER_CONFIG_TABLE.put(key, alarmConfig);
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
