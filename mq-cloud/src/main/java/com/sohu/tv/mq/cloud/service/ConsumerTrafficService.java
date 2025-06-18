package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.ConsumerTraffic;
import com.sohu.tv.mq.cloud.dao.ConsumerTrafficDao;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.util.ThreadPoolUtil;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.stats.Stats;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

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

    private ThreadPoolExecutor consumerTrafficFetchThreadPool =
            ThreadPoolUtil.createBlockingFixedThreadPool("consumerTrafficFetch", 2);

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
    public Pair<Integer, Integer> collectTraffic(Cluster mqCluster) {
        Result<List<Consumer>> consumerListResult = consumerService.queryConsumerList(mqCluster);
        if (consumerListResult.isEmpty()) {
            logger.warn("cannot get consumer list for cluster:{}", mqCluster);
            return new Pair<>(0, 0);
        }
        String time = DateUtil.getFormatNow(DateUtil.HHMM);
        List<Consumer> consumerList = consumerListResult.getResult();
        AtomicInteger fetchErrorCount = new AtomicInteger();
        List<Future> futures = new ArrayList<>();
        for (Consumer consumer : consumerList) {
            Future future = consumerTrafficFetchThreadPool.submit(() -> {
                try {
                    if (!collectTraffic(consumer, time, mqCluster)) {
                        fetchErrorCount.incrementAndGet();
                    }
                } catch (Throwable e) {
                    logger.error("collect traffic err, consumer:{}, time:{}, cluster:{}", consumer, time, mqCluster, e);
                }
            });
            futures.add(future);
        }
        // 等待所有任务执行完毕
        for (Future future : futures) {
            try {
                future.get();
            } catch (Throwable e) {
                logger.error("collect traffic err", e);
            }
        }
        // 保存broker流量
        brokerTrafficService.saveConsumeBrokerTraffic(time, mqCluster.getId());
        // 第一个参数表示topic size，第二个参数表示收集失败的数量
        Pair<Integer, Integer> pair = new Pair<>(0, 0);
        pair.setObject1(consumerList.size());
        pair.setObject2(fetchErrorCount.get());
        return pair;
    }

    /**
     * 收集流量
     */
    private Boolean collectTraffic(Consumer consumer, String time, Cluster mqCluster) {
        return mqAdminTemplate.execute(new DefaultCallback<Boolean>() {
            public Boolean callback(MQAdminExt mqAdmin) {
                String statKey = consumer.getTopicName() + "@" + consumer.getName();
                ConsumerTraffic consumerTraffic = new ConsumerTraffic();
                consumerTraffic.setCreateTime(time);
                consumerTraffic.setClusterId(mqCluster().getId());
                boolean hasFetchError = fetchTraffic(mqAdmin, consumer.getTopicName(), statKey, consumerTraffic);
                // 有数据才保存
                if (consumerTraffic.getCount() != 0 || consumerTraffic.getSize() != 0) {
                    consumerTraffic.setConsumerId(consumer.getId());
                    save(consumerTraffic);
                }
                return hasFetchError;
            }

            public Cluster mqCluster() {
                return mqCluster;
            }
        });
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
        return Stats.GROUP_GET_NUMS;
    }

    @Override
    protected String getSizeKey() {
        return Stats.GROUP_GET_SIZE;
    }

    @Override
    public Result<List<ConsumerTraffic>> query(long id, Date date) {
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
    public Result<List<ConsumerTraffic>> query(Collection<Long> idList, Date date) {
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

    public Result<ConsumerTraffic> queryTotalTraffic(List<Long> idList, Date date) {
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
    public Result<List<ConsumerTraffic>> query(List<Long> idList, Date date, String time) {
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
        brokerTraffic.setGetCount(traffic.getCurCount());
        brokerTraffic.setGetSize(traffic.getCurSize());
        brokerTraffic.setClusterId(traffic.getClusterId());
        brokerTrafficService.aggragateConsumeTraffic(brokerTraffic);
    }
}
