package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.Traffic;
import com.sohu.tv.mq.cloud.cache.LocalCache;
import com.sohu.tv.mq.cloud.util.Result;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.protocol.body.BrokerStatsData;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * 流量服务
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月27日
 */
public abstract class TrafficService<T extends Traffic> {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String ERROR = "e";

    @Autowired
    private TopicService topicService;

    @Autowired
    private LocalCache<String> fetchLocalCache;

    /**
     * 抓取topic流量
     * 
     * @param Topic
     * @param mqAdmin
     */
    protected void fetchTraffic(MQAdminExt mqAdmin, String topic, String statKey, T traffic) {
        // 获取topic路由
        TopicRouteData topicRouteData = null;
        try {
            topicRouteData = topicService.route(mqAdmin, topic);
        } catch (Exception e) {
            logger.warn("topic:{} route err", topic, e);
        }
        if (topicRouteData == null) {
            return;
        }
        // 获取broker数据
        List<BrokerData> brokerList = topicRouteData.getBrokerDatas();
        for (BrokerData brokerData : brokerList) {
            // 获取broker master
            String masterAddr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
            if (masterAddr == null) {
                continue;
            }
            String key = masterAddr + "_" + statKey + "_" + getCountKey();
            String value = fetchLocalCache.get(key);
            if (value != null && ERROR.equals(value)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("key:{} add to blacklist, not fetch!", key);
                }
                continue;
            }
            if(getCountKey() != null) {
                try {
                    // 获取次数统计
                    BrokerStatsData brokerPutStatsData = mqAdmin.viewBrokerStatsData(masterAddr, getCountKey(), statKey);
                    traffic.addCount(brokerPutStatsData);
                } catch (Exception e) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("fetch traffic, broker:{}, stat:{}, key:{}, err:{}", masterAddr, getCountKey(), statKey,
                                e.toString());
                    }
                    fetchLocalCache.put(key, ERROR);
                    continue;
                }
            }
            if(getSizeKey() != null) {
                try {
                    // 获取大小统计
                    BrokerStatsData brokerSizeStatsData = mqAdmin.viewBrokerStatsData(masterAddr, getSizeKey(), statKey);
                    traffic.addSize(brokerSizeStatsData);
                } catch (Exception e) {
                    if(logger.isDebugEnabled()) {
                        logger.debug("fetch traffic, broker:{}, stat:{}, key:{}, err:{}", masterAddr, getSizeKey(),
                                statKey, e.getMessage());
                    }
                }
            }
            
            // 处理流量
            processBrokerTraffic(masterAddr, traffic);
        }
    }
    
    /**
     * 获取需要统计的次数key
     * 
     * @return
     */
    protected abstract String getCountKey();

    /**
     * 获取需要统计的大小key
     * 
     * @return
     */
    protected abstract String getSizeKey();

    /**
     * 删除数据
     * 
     * @param date
     * @return 删除的行数
     */
    public abstract Result<Integer> delete(Date date);

    /**
     * 查询数据
     * 
     * @param id
     * @param date
     * @return Result<List<T>>
     */
    public abstract Result<List<T>> query(long id, Date date);

    /**
     * 查询数据
     * 
     * @param idList
     * @param date
     * @return Result<List<T>>
     */
    public abstract Result<List<T>> query(List<Long> idList, Date date);

    /**
     * 查询数据
     * 
     * @param idList
     * @param date
     * @param time
     * @return Result<List<T>>
     */
    public abstract Result<List<T>> query(List<Long> idList, Date date, String time);
    
    /**
     * 处理traffic数据
     * @param ip: broker ip
     * @param traffic: 流量
     */
    protected void processBrokerTraffic(String ip, T traffic) {
        //默认空实现，子类可覆盖
    }
}
