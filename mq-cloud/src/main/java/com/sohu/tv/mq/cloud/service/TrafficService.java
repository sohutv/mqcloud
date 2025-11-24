package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Traffic;
import com.sohu.tv.mq.cloud.cache.LocalCache;
import com.sohu.tv.mq.cloud.util.Result;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.protocol.body.BrokerStatsData;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
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

    @Autowired
    private BrokerService brokerService;

    /**
     * 抓取topic流量
     *
     * @param Topic
     * @param mqAdmin
     */
    protected boolean fetchTraffic(Cluster cluster, String topic, String statKey, T traffic) {
        // 获取topic路由信息
        TopicRouteData topicRouteData = fetchTopicRouteData(cluster, topic);
        if (topicRouteData == null) {
            logger.warn("cannot get topic route data, cluster:{}, topic:{}", cluster, topic);
            return false;
        }
        boolean hasFetchError = false;
        for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
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
                hasFetchError = true;
                continue;
            }
            if (getCountKey() != null) {
                Result<BrokerStatsData> result = brokerService.viewBrokerStatsData(cluster, masterAddr, getCountKey(), statKey, false);
                if (result.getException() != null) {
                    fetchLocalCache.put(key, ERROR);
                    hasFetchError = true;
                    continue;
                }
                traffic.addCount(result.getResult());
            }
            if (getSizeKey() != null) {
                Result<BrokerStatsData> result = brokerService.viewBrokerStatsData(cluster, masterAddr, getSizeKey(), statKey, false);
                if (result.getException() != null) {
                    hasFetchError = true;
                }
                if (result.isOK()) {
                    traffic.addSize(result.getResult());
                }
            }
            // 处理流量
            processBrokerTraffic(masterAddr, traffic);
        }
        return hasFetchError;
    }


    private TopicRouteData fetchTopicRouteData(Cluster mqCluster, String topic) {
        for (int i = 0; i < 3; i++) {
            TopicRouteData topicRouteData = topicService.route(mqCluster, topic);
            if (topicRouteData != null) {
                return topicRouteData;
            }
            logger.warn("fetch topic route data failed, cluster:{}, topic:{}, retry:{}", mqCluster, topic, (i + 1));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        return null;
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
    public abstract Result<List<T>> query(Collection<Long> idList, Date date);

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
