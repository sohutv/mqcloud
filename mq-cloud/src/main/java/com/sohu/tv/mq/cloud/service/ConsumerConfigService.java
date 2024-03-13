package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.ConsumerConfig;
import com.sohu.tv.mq.cloud.bo.ConsumerPauseConfig;
import com.sohu.tv.mq.cloud.dao.ConsumerConfigDao;
import com.sohu.tv.mq.cloud.dao.ConsumerPauseConfigDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.*;

/**
 * 消费者配置服务
 * 
 * @author yongfeigao
 * @date 2020年6月3日
 */
@Service
public class ConsumerConfigService implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConsumerConfigDao consumerConfigDao;

    @Autowired
    private ConsumerPauseConfigDao consumerPauseConfigDao;

    private ConcurrentMap<String, ConsumerConfig> consumerConfigMap = new ConcurrentHashMap<>();

    public ConsumerConfigService() {

    }

    public ConsumerConfig getConsumerConfig(String consumer) {
        return consumerConfigMap.get(consumer);
    }

    /**
     * 保存
     *
     * @param consumerConfig
     * @return 返回Result
     */
    public Result<?> save(ConsumerConfig consumerConfig) {
        try {
            consumerConfigDao.insert(consumerConfig);
        } catch (Exception e) {
            logger.error("insert err, consumerConfig:{}", consumerConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 保存
     * 
     * @param consumerConfig
     * @return 返回Result
     */
    @Transactional
    public Result<?> savePause(ConsumerConfig consumerConfig) {
        try {
            consumerConfigDao.insert(consumerConfig);
            Entry<String, Boolean> entry = consumerConfig.findPauseConfigFirstEntry();
            if (entry != null) {
                // 暂停某个客户端
                ConsumerPauseConfig consumerPauseConfig = new ConsumerPauseConfig();
                consumerPauseConfig.setConsumer(consumerConfig.getConsumer());
                consumerPauseConfig.setPauseClientId(entry.getKey());
                consumerPauseConfig.setUnregister(entry.getValue());
                consumerPauseConfigDao.insert(consumerPauseConfig);
            }
        } catch (Exception e) {
            logger.error("insert err, consumerConfig:{}", consumerConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 恢复暂停
     * @param consumerConfig
     * @return
     */
    @Transactional
    public Result<?> saveResume(ConsumerConfig consumerConfig) {
        try {
            Entry<String, Boolean> entry = consumerConfig.findPauseConfigFirstEntry();
            if (entry != null) {
                // 恢复某个客户端
                consumerPauseConfigDao.delete(consumerConfig.getConsumer(), entry.getKey());
                List<ConsumerPauseConfig> list = consumerPauseConfigDao.selectByConsumer(consumerConfig.getConsumer());
                // 如果没有暂停的客户端，重置为正常状态
                if (list == null || list.size() == 0) {
                    consumerConfigDao.insert(consumerConfig);
                }
            } else {
                // 恢复所有客户端
                consumerPauseConfigDao.deleteByConsumer(consumerConfig.getConsumer());
                consumerConfigDao.insert(consumerConfig);
            }
        } catch (Exception e) {
            logger.error("insert err, consumerConfig:{}", consumerConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 更新
     */
    public void update() {
        try {
            // 获取所有消费者配置
            List<ConsumerConfig> consumerConfigList = consumerConfigDao.selectAll();
            if (consumerConfigList == null || consumerConfigList.size() == 0) {
                consumerConfigMap.clear();
                return;
            }
            // 获取所有消费者暂停配置
            List<ConsumerPauseConfig> consumerPauseConfigs = consumerPauseConfigDao.selectAll();
            for (ConsumerConfig consumerConfig : consumerConfigList) {
                // 更新暂停配置
                if (!CollectionUtils.isEmpty(consumerPauseConfigs)) {
                    String consumer = consumerConfig.getConsumer();
                    consumerPauseConfigs.stream().filter(c -> c.getConsumer().equals(consumer)).forEach(c -> {
                        consumerConfig.addPauseConfig(c.getPauseClientId(), c.getUnregister());
                    });
                }
                consumerConfigMap.put(consumerConfig.getConsumer(), consumerConfig);
            }
        } catch (Exception e) {
            logger.error("query all err", e);
        }
    }

    /**
     * 定时更新
     */
    public void afterPropertiesSet() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "updateConsumerConfigThread");
            }
        }).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
}
