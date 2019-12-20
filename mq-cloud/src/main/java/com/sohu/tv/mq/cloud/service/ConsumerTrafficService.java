package com.sohu.tv.mq.cloud.service;

import java.util.Date;
import java.util.List;

import org.apache.rocketmq.store.stats.BrokerStatsManager;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.ConsumerTraffic;
import com.sohu.tv.mq.cloud.dao.ConsumerTrafficDao;
import com.sohu.tv.mq.cloud.mq.DefaultInvoke;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;

/**
 * consumer流量服务
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月27日
 */
@Service
public class ConsumerTrafficService extends TrafficService<ConsumerTraffic> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConsumerTrafficDao consumerTrafficDao;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private BrokerTrafficService brokerTrafficService;

    /**
     * 保存consumer流量
     * 
     * @param topicTraffic
     * @return
     */
    public Result<Status> save(ConsumerTraffic consumerTraffic) {
        try {
            consumerTrafficDao.insert(consumerTraffic);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", consumerTraffic);
            return Result.getResult(Status.DB_DUPLICATE_KEY);
        } catch (Exception e) {
            logger.error("insert err, consumerTraffic:{}", consumerTraffic, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 收集集群的流量
     * 
     * @param mqCluster
     * @return topic size
     */
    public int collectTraffic(Cluster mqCluster) {
        Result<List<Consumer>> consumerListResult = consumerService.queryConsumerList(mqCluster);
        if (consumerListResult.isEmpty()) {
            logger.warn("cannot get consumer list for cluster:{}", mqCluster);
            return 0;
        }
        String time = DateUtil.getFormatNow(DateUtil.HHMM);
        List<Consumer> consumerList = consumerListResult.getResult();
        mqAdminTemplate.execute(new DefaultInvoke() {
            public void invoke(MQAdminExt mqAdmin) {
                for (Consumer consumer : consumerList) {
                    String statKey = consumer.getTopicName() + "@" + consumer.getName();
                    ConsumerTraffic consumerTraffic = new ConsumerTraffic();
                    consumerTraffic.setCreateTime(time);
                    consumerTraffic.setClusterId(mqCluster().getId());
                    fetchTraffic(mqAdmin, consumer.getTopicName(), statKey, consumerTraffic);
                    // 有数据才保存
                    if (consumerTraffic.getCount() != 0 || consumerTraffic.getSize() != 0) {
                        consumerTraffic.setConsumerId(consumer.getId());
                        save(consumerTraffic);
                    }
                }
            }

            public Cluster mqCluster() {
                return mqCluster;
            }
        });
        // 保存broker流量
        brokerTrafficService.saveConsumeBrokerTraffic(time, mqCluster.getId());
        return consumerList.size();
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
            rows = consumerTrafficDao.delete(date);
        } catch (Exception e) {
            logger.error("dete err, date:{}", date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(rows);
    }

    @Override
    protected String getCountKey() {
        return BrokerStatsManager.GROUP_GET_NUMS;
    }

    @Override
    protected String getSizeKey() {
        return BrokerStatsManager.GROUP_GET_SIZE;
    }

    @Override
    public Result<List<ConsumerTraffic>> query(long id, String date) {
        List<ConsumerTraffic> list = null;
        try {
            list = consumerTrafficDao.select(id, date);
        } catch (Exception e) {
            logger.error("query traffic err, id:{},date:{}", id, date);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    @Override
    public Result<List<ConsumerTraffic>> query(List<Long> idList, String date) {
        if (idList == null || idList.size() == 0) {
            return Result.getResult(Status.NO_RESULT);
        }
        List<ConsumerTraffic> list = null;
        try {
            list = consumerTrafficDao.selectByIdList(idList, date);
        } catch (Exception e) {
            logger.error("query traffic err, idList:{},date:{}", idList, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    public Result<ConsumerTraffic> queryTotalTraffic(List<Long> idList, String date) {
        ConsumerTraffic consumerTraffic = null;
        try {
            consumerTraffic = consumerTrafficDao.selectTotalTraffic(idList, date);
        } catch (Exception e) {
            logger.error("queryTotalTraffic err, idList:{},date:{}", idList, date);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(consumerTraffic);
    }

    @Override
    public Result<List<ConsumerTraffic>> query(List<Long> idList, String date, String time) {
        if (idList == null || idList.size() == 0) {
            return Result.getResult(Status.NO_RESULT);
        }
        List<ConsumerTraffic> list = null;
        try {
            list = consumerTrafficDao.selectByIdListDateTime(idList, date, time);
        } catch (Exception e) {
            logger.error("query traffic err, idList:{},date:{},time:{}", idList, date, time);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    @Override
    protected void processBrokerTraffic(String ip, ConsumerTraffic traffic) {
        if (traffic.getCount() == 0 && traffic.getSize() == 0) {
            return;
        }
        BrokerTraffic brokerTraffic = new BrokerTraffic();
        brokerTraffic.setIp(ip);
        brokerTraffic.setCreateTime(traffic.getCreateTime());
        brokerTraffic.setGetCount(traffic.getCount());
        brokerTraffic.setGetSize(traffic.getSize());
        brokerTraffic.setClusterId(traffic.getClusterId());
        brokerTrafficService.aggragateConsumeTraffic(brokerTraffic);
    }
}
