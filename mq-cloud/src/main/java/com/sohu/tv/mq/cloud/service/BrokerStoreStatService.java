package com.sohu.tv.mq.cloud.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.common.model.BrokerStoreStat;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.dao.BrokerStoreStatDao;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * broker存储统计
 * 
 * @author yongfeigao
 * @date 2020年4月26日
 */
@Service
public class BrokerStoreStatService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BrokerStoreStatDao brokerStoreStatDao;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    /**
     * 保存BrokerStoreStat记录
     * 
     * @param brokerStoreStat
     * @return 返回Result
     */
    public Result<Integer> save(BrokerStoreStat brokerStoreStat) {
        Integer result = null;
        try {
            result = brokerStoreStatDao.insert(brokerStoreStat);
        } catch (Exception e) {
            logger.error("insert err, brokerStoreStat:{}", brokerStoreStat, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 查询记录
     * 
     * @param producer
     * @param date
     * @return
     */
    public Result<List<BrokerStoreStat>> query(String brokerIp, Date date) {
        List<BrokerStoreStat> result = null;
        int dt = NumberUtils.toInt(DateUtil.formatYMD(date));
        try {
            result = brokerStoreStatDao.selectByDate(brokerIp, dt);
        } catch (Exception e) {
            logger.error("query err, brokerIp:{}, date:{}, dt:{}", brokerIp, date, dt, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 删除记录
     * 
     * @param date
     * @return 返回Result
     */
    public Result<Integer> delete(Date date) {
        int dt = NumberUtils.toInt(DateUtil.formatYMD(date));
        Integer result = null;
        try {
            result = brokerStoreStatDao.delete(dt);
        } catch (Exception e) {
            logger.error("delete err, date:{}, dt:{}", date, dt, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 抓取broker存储统计
     * @param mqCluster
     * @param brokerAddr
     * @return
     */
    public Result<BrokerStoreStat> fetchBrokerStoreStat(Cluster mqCluster, String brokerAddr) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<BrokerStoreStat>>() {
            public Result<BrokerStoreStat> callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                return Result.getResult(sohuMQAdmin.getBrokerStoreStats(brokerAddr));
            }

            public Result<BrokerStoreStat> exception(Exception e) throws Exception {
                logger.warn("brokerAddr:{} error:{}", brokerAddr, e.getMessage());
                return Result.getDBErrorResult(e);
            }

            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }
}
