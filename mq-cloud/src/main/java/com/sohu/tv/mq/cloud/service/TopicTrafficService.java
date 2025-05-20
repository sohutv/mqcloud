package com.sohu.tv.mq.cloud.service;

import com.google.common.collect.Lists;
import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.dao.TopicTrafficDao;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.stats.Stats;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * topic流量服务
 *
 * @author yongfeigao
 * @Description:
 * @date 2018年6月26日
 */
@Service
public class TopicTrafficService extends TrafficService<TopicTraffic> {
    @Autowired
    private TopicTrafficDao topicTrafficDao;

    @Autowired
    private TopicService topicService;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private BrokerTrafficService brokerTrafficService;

    @Autowired
    private SqlSessionFactory mqSqlSessionFactory;

    private ThreadPoolExecutor topicTrafficFetchThreadPool =
            ThreadPoolUtil.createBlockingFixedThreadPool("topicTrafficFetch", 2);

    /**
     * 保存topic流量
     *
     * @param topicTraffic
     * @return
     */
    public Result<TopicTraffic> save(TopicTraffic topicTraffic) {
        try {
            topicTrafficDao.insert(topicTraffic);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", topicTraffic);
            return Result.getResult(Status.DB_DUPLICATE_KEY);
        } catch (Exception e) {
            logger.error("insert err, topicTraffic:{}", topicTraffic, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicTraffic);
    }

    /**
     * 收集集群的流量
     *
     * @param mqCluster
     * @return topic size
     */
    public Pair<Integer, Integer> collectTraffic(Cluster mqCluster) {
        Result<List<Topic>> topicListResult = topicService.queryTopicList(mqCluster);
        if (topicListResult.isEmpty()) {
            logger.error("cannot get topic list for cluster:{}", mqCluster);
            return new Pair<>(0, 0);
        }
        String time = DateUtil.getFormatNow(DateUtil.HHMM);
        Date date = new Date();
        AtomicInteger fetchErrorCount = new AtomicInteger();
        List<Future> futures = new ArrayList<>();
        List<Topic> topicList = topicListResult.getResult();
        for (Topic topic : topicList) {
            Future future = topicTrafficFetchThreadPool.submit(() -> {
                try {
                    if (!collectTraffic(topic, date, time, mqCluster)) {
                        fetchErrorCount.incrementAndGet();
                    }
                } catch (Throwable e) {
                    logger.error("collect traffic err, topic:{}, time:{}, cluster:{}", topic, time, mqCluster, e);
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
        brokerTrafficService.saveProduceBrokerTraffic(time, mqCluster.getId());
        // 第一个参数表示topic size，第二个参数表示收集失败的数量
        Pair<Integer, Integer> pair = new Pair<>(0, 0);
        pair.setObject1(topicList.size());
        pair.setObject2(fetchErrorCount.get());
        return pair;
    }

    /**
     * 收集流量
     */
    private Boolean collectTraffic(Topic topic, Date date, String time, Cluster mqCluster) {
        return mqAdminTemplate.execute(new DefaultCallback<Boolean>() {
            public Boolean callback(MQAdminExt mqAdmin) throws Exception {
                TopicTraffic topicTraffic = new TopicTraffic();
                topicTraffic.setCreateTime(time);
                topicTraffic.setCreateDate(date);
                topicTraffic.setClusterId(mqCluster().getId());
                boolean hasFetchError = fetchTraffic(mqAdmin, topic.getName(), topic.getName(), topicTraffic);
                if (topicTraffic.getCount() != 0 || topicTraffic.getSize() != 0) {
                    topicTraffic.setTid(topic.getId());
                    save(topicTraffic);
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
            rows = topicTrafficDao.delete(date);
        } catch (Exception e) {
            logger.error("dete err, date:{}", date, e);
            return Result.getDBErrorResult(e);
        }
        // 在这里耦合一下吧，删除broker数据
        brokerTrafficService.delete(date);
        return Result.getResult(rows);
    }

    @Override
    protected String getCountKey() {
        return Stats.TOPIC_PUT_NUMS;
    }

    @Override
    protected String getSizeKey() {
        return Stats.TOPIC_PUT_SIZE;
    }

    @Override
    public Result<List<TopicTraffic>> query(long id, Date date) {
        List<TopicTraffic> list = null;
        try {
            list = topicTrafficDao.select(id, date);
        } catch (Exception e) {
            logger.error("query traffic err, id:{},date:{}", id, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    public Result<TopicTraffic> queryTotalTraffic(long id, Date date) {
        TopicTraffic topicTraffic = null;
        try {
            topicTraffic = topicTrafficDao.selectTotalTraffic(id, date);
        } catch (Exception e) {
            logger.error("queryTotalTraffic err, id:{},date:{}", id, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicTraffic);
    }

    @Override
    public Result<List<TopicTraffic>> query(List<Long> idList, Date date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Result<List<TopicTraffic>> query(List<Long> idList, Date date, String time) {
        List<TopicTraffic> list = null;
        try {
            list = topicTrafficDao.selectByIdListDateTime(idList, date, time);
        } catch (Exception e) {
            logger.error("query traffic err, idList:{},date:{},time:{}", idList, date, time, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    public Result<TopicTraffic> query(long tid, Date date, String time) {
        try {
            List<TopicTraffic> list = topicTrafficDao.selectByIdListDateTime(Lists.newArrayList(tid), date, time);
            if (list != null && list.size() > 0) {
                return Result.getResult(list.get(0));
            } else {
                return Result.getResult(Status.NO_RESULT);
            }
        } catch (Exception e) {
            logger.error("query traffic err, tid:{},date:{},time:{}", tid, date, time, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询小于某一日期的所有流量
     *
     * @param tid
     * @param createDate
     * @return
     */
    public Result<List<TopicTraffic>> queryRangeTraffic(long tid, Date createDate) {
        List<TopicTraffic> list = null;
        try {
            list = topicTrafficDao.selectRangeTraffic(tid, createDate);
        } catch (Exception e) {
            logger.error("queryRangeTraffic err,tid:{},createDate:{}", tid, createDate, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 查询指定一天时间内的流量
     */
    public Result<List<TopicTraffic>> queryRangeTraffic(long tid, Map<String, List<String>> timeMap) {
        List<TopicTraffic> list = new ArrayList<>();
        try {
            for (Map.Entry<String, List<String>> entry : timeMap.entrySet()) {
                List<TopicTraffic> topicTrafficList = topicTrafficDao.selectByCreateDateAndTime(tid, entry.getKey(), entry.getValue());
                if (topicTrafficList != null) {
                    list.addAll(topicTrafficList);
                }
            }
            return Result.getResult(list);
        } catch (Exception e) {
            logger.error("queryRangeTraffic err, tid:{}, timeMap:{}", tid, timeMap, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询时间段内的流量
     */
    public Result<List<TopicTraffic>> query(Map<String, List<String>> timeMap) {
        List<TopicTraffic> list = new ArrayList<>();
        try {
            for (Map.Entry<String, List<String>> entry : timeMap.entrySet()) {
                List<TopicTraffic> topicTrafficList = topicTrafficDao.selectByDateTime(entry.getKey(), entry.getValue());
                if (topicTrafficList != null) {
                    list.addAll(topicTrafficList);
                }
            }
            return Result.getResult(list);
        } catch (Exception e) {
            logger.error("query traffic err, timeMap:{}", timeMap, e);
            return Result.getDBErrorResult(e);
        }
    }

    @Override
    protected void processBrokerTraffic(String ip, TopicTraffic traffic) {
        if (traffic.getCount() == 0 && traffic.getSize() == 0) {
            return;
        }
        BrokerTraffic brokerTraffic = new BrokerTraffic();
        brokerTraffic.setIp(ip);
        brokerTraffic.setCreateTime(traffic.getCreateTime());
        brokerTraffic.setPutCount(traffic.getCurCount());
        brokerTraffic.setPutSize(traffic.getCurSize());
        brokerTraffic.setClusterId(traffic.getClusterId());
        brokerTrafficService.aggragateProduceTraffic(brokerTraffic);
    }

    /**
     * 更新topic日流量
     *
     * @return
     */
    public Result<?> updateTopicDayTraffic() {
        // 先重置
        topicService.resetDayCount();
        long now = System.currentTimeMillis();
        Date day7Ago = new Date(now - 7L * 24 * 60 * 60 * 1000);
        Date day1Ago = new Date(now - 1L * 24 * 60 * 60 * 1000);
        int updateCount = 0;
        int size = 1000;
        int offset = 0;
        while (true) {
            List<TopicTraffic> topicTrafficList = topicTrafficDao.selectSummary(day7Ago, day1Ago, offset, size);
            if (topicTrafficList == null || topicTrafficList.size() == 0) {
                logger.warn("selectSummarySize no data, day7Ago:{}, day1Agao:{}, offset:{}, size:{}", day7Ago, day1Ago,
                        offset, size);
                break;
            }
            long start = System.currentTimeMillis();
            List<TopicTraffic> result = aggregateTopicTraffic(topicTrafficList);
            Result<Integer> updateResult = topicService.updateDayCount(result);
            if (updateResult.isOK()) {
                logger.warn("updateDayCount size:{}, use:{}ms", updateResult.getResult(), System.currentTimeMillis() - start);
                updateCount += updateResult.getResult();
            } else {
                logger.warn("updateDayCount error:{}", updateResult);
                break;
            }
            if (topicTrafficList.size() < size) {
                break;
            }
            offset += size;
        }
        return Result.getResult(updateCount);
    }

    /**
     * 聚合topic流量
     *
     * @param topicTrafficList
     * @return
     */
    private List<TopicTraffic> aggregateTopicTraffic(List<TopicTraffic> topicTrafficList) {
        Date today = new Date();
        List<TopicTraffic> result = Lists.newArrayList();
        for (TopicTraffic topicTraffic : topicTrafficList) {
            // 查找是否已经存在
            TopicTraffic destTopicTraffic = null;
            for (TopicTraffic traffic : result) {
                if (traffic.getTid() == topicTraffic.getTid()) {
                    destTopicTraffic = traffic;
                    break;
                }
            }
            int day = DateUtil.daysBetween(today, topicTraffic.getCreateDate());
            if (destTopicTraffic == null) {
                setDaySize(topicTraffic, day, topicTraffic.getSize());
                setDayCount(topicTraffic, day, topicTraffic.getCount());
                result.add(topicTraffic);
            } else {
                setDaySize(destTopicTraffic, day, topicTraffic.getSize());
                setDayCount(destTopicTraffic, day, topicTraffic.getCount());
            }
        }
        return result;
    }

    /**
     * 设置天数流量，size1d表示1天前的流量
     *
     * @param topicTraffic
     * @param day
     * @param size
     */
    private void setDaySize(TopicTraffic topicTraffic, int day, long size) {
        switch (day) {
            case 1:
                topicTraffic.addSize1d(size);
            case 2:
                topicTraffic.addSize2d(size);
            case 3:
                topicTraffic.addSize3d(size);
            case 4:
            case 5:
                topicTraffic.addSize5d(size);
            case 6:
            case 7:
                topicTraffic.addSize7d(size);
        }
    }

    /**
     * 设置天数流量，count1d表示1天前的流量
     */
    private void setDayCount(TopicTraffic topicTraffic, int day, long count) {
        switch (day) {
            case 1:
                topicTraffic.addCount1d(count);
            case 2:
                topicTraffic.addCount2d(count);
        }
    }

    /**
     * 批量插入数据
     *
     * @param topicTrafficList
     * @return
     */
    public Result<Integer> batchInsert(List<TopicTraffic> topicTrafficList) {
        return DBUtil.batchUpdate(mqSqlSessionFactory, TopicTrafficDao.class, dao -> {
            for (TopicTraffic topicTraffic : topicTrafficList) {
                dao.insert(topicTraffic);
            }
        });
    }

    public Result<Long> queryTopicSummarySize(long tid, Date begin, Date end) {
        try {
            return Result.getResult(topicTrafficDao.selectTopicSummarySize(tid, begin, end));
        } catch (Exception e) {
            logger.error("queryTopicSummarySize err, tid:{},begin:{},end:{}", tid, begin, end, e);
            return Result.getDBErrorResult(e);
        }
    }
}
