package com.sohu.tv.mq.cloud.service;

import com.google.common.collect.Lists;
import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.cache.LocalCache;
import com.sohu.tv.mq.cloud.dao.BrokerTrafficDao;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * broker流量服务
 * 
 * @author yongfeigao
 * @date 2018年9月28日
 */
@Service
public class BrokerTrafficService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public static final String PRODUCE_FLAG = "produce";
    
    public static final String CONSUME_FLAG = "consume";

    @Autowired
    private BrokerTrafficDao brokerTrafficDao;

    @Autowired
    private LocalCache<Map<String, BrokerTraffic>> trafficLocalCache;

    @Autowired
    private BrokerService brokerService;

    /**
     * 聚合生产流量
     * 
     * @param brokerTraffic
     * @return
     */
    public void aggragateProduceTraffic(BrokerTraffic brokerTraffic) {
        aggragate(PRODUCE_FLAG, brokerTraffic);
    }
    
    /**
     * 聚合消费流量
     * 
     * @param brokerTraffic
     * @return
     */
    public void aggragateConsumeTraffic(BrokerTraffic brokerTraffic) {
        aggragate(CONSUME_FLAG, brokerTraffic);
    }
    
    /**
     * 聚合流量
     * 
     * @param flag 流量标识，来自于哪
     * @param brokerTraffic
     * @return
     */
    private void aggragate(String flag, BrokerTraffic brokerTraffic) {
        String cacheKey = buildKey(flag, brokerTraffic.getCreateTime(), brokerTraffic.getClusterId());
        Map<String, BrokerTraffic> map = trafficLocalCache.get(cacheKey);
        if (map == null) {
            map = new HashMap<String, BrokerTraffic>();
            trafficLocalCache.put(cacheKey, map);
        }
        BrokerTraffic prevBrokerTraffic = map.get(brokerTraffic.getIp());
        if (prevBrokerTraffic == null) {
            map.put(brokerTraffic.getIp(), brokerTraffic);
        } else {
            prevBrokerTraffic.add(brokerTraffic);
        }
    }
    
    private String buildKey(String flag, String time, int clusterId){
        return flag + "_" + time + "_" + clusterId;
    }

    public void saveProduceBrokerTraffic(String time, int clusterId) {
        save(buildKey(PRODUCE_FLAG, time, clusterId));
    }
    
    public void saveConsumeBrokerTraffic(String time, int clusterId) {
        save(buildKey(CONSUME_FLAG, time, clusterId));
    }
    
    public void save(String key) {
        Map<String, BrokerTraffic> map = trafficLocalCache.get(key);
        if(map == null) {
            if(logger.isDebugEnabled()) {
                logger.debug("key:{} traffic is null!", key);
            }
            return;
        }
        long start = System.currentTimeMillis();
        for(BrokerTraffic brokerTraffic : map.values()) {
            save(brokerTraffic);
        }
        logger.info("save broker traffic, key:{} size:{} use:{}ms", key, map.size(), (System.currentTimeMillis() - start));
    }

    /**
     * 保存流量
     * 
     * @param brokerTraffic
     * @return
     */
    public Result<?> save(BrokerTraffic brokerTraffic) {
        try {
            brokerTrafficDao.insert(brokerTraffic);
        } catch (Exception e) {
            logger.error("insert err, brokerTraffic:{}", brokerTraffic, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 删除数据
     * 
     * @param date
     * @return
     */
    public Result<Integer> delete(Date date) {
        Integer rows = 0;
        try {
            rows = brokerTrafficDao.delete(date);
        } catch (Exception e) {
            logger.error("dete err, date:{}", date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(rows);
    }

    /**
     * 查询流量
     * 
     * @param ip
     * @param date
     * @return
     */
    public Result<List<BrokerTraffic>> query(String ip, Date date) {
        List<BrokerTraffic> list = null;
        try {
            list = brokerTrafficDao.select(ip, date);
        } catch (Exception e) {
            logger.error("query traffic err, ip:{},date:{}", ip, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 查询流量
     * 
     * @param ip
     * @param date
     * @return
     */
    public Result<List<BrokerTraffic>> queryClusterTraffic(int clusterId, Date date) {
        List<BrokerTraffic> list = null;
        try {
            list = brokerTrafficDao.selectClusterTraffic(clusterId, date);
        } catch (Exception e) {
            logger.error("query cluster traffic err, clusterId:{},date:{}", clusterId, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
    
    /**
     * 查询流量
     * 
     * @param ip
     * @param date
     * @return
     */
    public Result<List<BrokerTraffic>> queryTraffic(Date date, List<String> times, List<String> ips) {
        List<BrokerTraffic> list = null;
        try {
            list = brokerTrafficDao.selectTrafficList(date, times, ips);
        } catch (Exception e) {
            logger.error("query traffic err, ips:{}, times:{}, date:{}", ips, times, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
    
    /**
     * 查询统计流量
     * 
     * @param ip
     * @param date
     * @return
     */
    public Result<BrokerTraffic> queryTrafficStatistic(Date date, List<String> ips, String beginTime) {
        BrokerTraffic brokerTraffic = null;
        try {
            brokerTraffic = brokerTrafficDao.selectTrafficStatistic(date, ips, beginTime);
        } catch (Exception e) {
            logger.error("query traffic err, ips:{}, beginTime:{}, date:{}", ips, beginTime, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(brokerTraffic);
    }
    
    /**
     * 查询统计流量
     * 
     * @param ip
     * @param date
     * @return
     */
    public Result<BrokerTraffic> queryTrafficStatistic(Date date, String ip, String beginTime) {
        BrokerTraffic brokerTraffic = null;
        try {
            brokerTraffic = brokerTrafficDao.selectTrafficStatisticByIp(date, ip, beginTime);
        } catch (Exception e) {
            logger.error("query traffic err, ip:{}, beginTime:{}, date:{}", ip, beginTime, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(brokerTraffic);
    }

    /**
     * 更新broker日流量
     *
     * @return
     */
    public Result<?> updateBrokerDayTraffic() {
        // 先重置
        brokerService.resetDayCount();
        long now = System.currentTimeMillis();
        Date day7Ago = new Date(now - 7L * 24 * 60 * 60 * 1000);
        Date day1Ago = new Date(now - 1L * 24 * 60 * 60 * 1000);
        List<BrokerTraffic> brokerTrafficList = brokerTrafficDao.selectSummarySize(day7Ago, day1Ago);
        if (brokerTrafficList == null || brokerTrafficList.size() == 0) {
            logger.warn("selectSummarySize no data, day7Ago:{}, day1Agao:{}", day7Ago, day1Ago);
            return Result.getResult(0);
        }
        long start = System.currentTimeMillis();
        List<BrokerTraffic> result = aggregateBrokerTraffic(brokerTrafficList);
        Result<Integer> updateResult = brokerService.updateDayCount(result);
        return updateResult;
    }

    /**
     * 聚合broker流量
     *
     * @param brokerTrafficList
     * @return
     */
    private List<BrokerTraffic> aggregateBrokerTraffic(List<BrokerTraffic> brokerTrafficList) {
        Date today = new Date();
        List<BrokerTraffic> result = Lists.newArrayList();
        for (BrokerTraffic brokerTraffic : brokerTrafficList) {
            // 查找是否已经存在
            BrokerTraffic destBrokerTraffic = null;
            for (BrokerTraffic traffic : result) {
                if (traffic.getIp() == brokerTraffic.getIp()) {
                    destBrokerTraffic = traffic;
                    break;
                }
            }
            if (destBrokerTraffic == null) {
                setDaySize(brokerTraffic, DateUtil.daysBetween(today, brokerTraffic.getCreateDate()), brokerTraffic.getPutSize());
                result.add(brokerTraffic);
            } else {
                setDaySize(destBrokerTraffic, DateUtil.daysBetween(today, brokerTraffic.getCreateDate()), brokerTraffic.getPutSize());
            }
        }
        return result;
    }

    /**
     * 设置天数流量，size1d表示1天前的流量
     *
     * @param brokerTraffic
     * @param day
     * @param size
     */
    private void setDaySize(BrokerTraffic brokerTraffic, int day, long size){
        switch (day) {
            case 1:
                brokerTraffic.addSize1d(size);
            case 2:
                brokerTraffic.addSize2d(size);
            case 3:
                brokerTraffic.addSize3d(size);
            case 4:
            case 5:
                brokerTraffic.addSize5d(size);
            case 6:
            case 7:
                brokerTraffic.addSize7d(size);
        }
    }
}
