package com.sohu.tv.mq.cloud.service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.ConsumerConfig;
import com.sohu.tv.mq.cloud.dao.ConsumerConfigDao;
import com.sohu.tv.mq.cloud.util.Result;

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
     * 更新
     */
    public int update() {
        try {
            List<ConsumerConfig> consumerConfigList = consumerConfigDao.selectAll();
            if (consumerConfigList == null || consumerConfigList.size() == 0) {
                return 0;
            }
            for (ConsumerConfig consumerConfig : consumerConfigList) {
                ConsumerConfig prev = consumerConfigMap.get(consumerConfig.getConsumer());
                if (!consumerConfig.equals(prev)) {
                    consumerConfigMap.put(consumerConfig.getConsumer(), consumerConfig);
                }
            }
            return consumerConfigList.size();
        } catch (Exception e) {
            logger.error("query all err", e);
        }
        return 0;
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
        }, 0, 60, TimeUnit.SECONDS);
    }
}
