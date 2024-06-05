package com.sohu.tv.mq.cloud.service;

import com.google.common.collect.Lists;
import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.dao.TopicTrafficDao;
import com.sohu.tv.mq.cloud.mq.DefaultInvoke;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.DBUtil;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.rocketmq.common.stats.Stats;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
    public int collectTraffic(Cluster mqCluster) {
        Result<List<Topic>> topicListResult = topicService.queryTopicList(mqCluster);
        if (topicListResult.isEmpty()) {
            logger.error("cannot get topic list for cluster:{}", mqCluster);
            return 0;
        }
        String time = DateUtil.getFormatNow(DateUtil.HHMM);
        Date date = new Date();
        List<Topic> topicList = topicListResult.getResult();
        mqAdminTemplate.execute(new DefaultInvoke() {
            public void invoke(MQAdminExt mqAdmin) throws Exception {
                for (Topic topic : topicList) {
                    TopicTraffic topicTraffic = new TopicTraffic();
                    topicTraffic.setCreateTime(time);
                    topicTraffic.setCreateDate(date);
                    topicTraffic.setClusterId(mqCluster().getId());
                    fetchTraffic(mqAdmin, topic.getName(), topic.getName(), topicTraffic);
                    if (topicTraffic.getCount() != 0 || topicTraffic.getSize() != 0) {
                        topicTraffic.setTid(topic.getId());
                        save(topicTraffic);
                    }
                }
            }

            public Cluster mqCluster() {
                return mqCluster;
            }
        });
        // 保存broker流量
        brokerTrafficService.saveProduceBrokerTraffic(time, mqCluster.getId());
        return topicList.size();
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
     *
     * @param tid
     * @param createDate
     * @param createTimeList
     * @return
     */
    public Result<List<TopicTraffic>> queryRangeTraffic(long tid, Date createDate, List<String> createTimeList) {
        List<TopicTraffic> list = null;
        try {
            list = topicTrafficDao.selectByCreateDateAndTime(tid, createDate, createTimeList);
        } catch (Exception e) {
            logger.error("queryRangeTraffic err,tid:{},createDate:{},createTimeList:{}", tid, createDate, createTimeList, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 查询时间段内的流量
     *
     * @param date
     * @param timeList
     * @return
     */
    public Result<List<TopicTraffic>> query(Date date, List<String> timeList) {
        List<TopicTraffic> list = null;
        try {
            list = topicTrafficDao.selectByDateTime(date, timeList);
        } catch (Exception e) {
            logger.error("query traffic err,date:{},timeList:{}", date, timeList, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
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
            List<TopicTraffic> topicTrafficList = topicTrafficDao.selectSummarySize(day7Ago, day1Ago, offset, size);
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
            if (destTopicTraffic == null) {
                setDaySize(topicTraffic, DateUtil.daysBetween(today, topicTraffic.getCreateDate()), topicTraffic.getSize());
                result.add(topicTraffic);
            } else {
                setDaySize(destTopicTraffic, DateUtil.daysBetween(today, topicTraffic.getCreateDate()), topicTraffic.getSize());
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
