package com.sohu.tv.mq.cloud.service;

import java.util.Date;
import java.util.List;

import org.apache.rocketmq.store.stats.BrokerStatsManager;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.dao.TopicTrafficDao;
import com.sohu.tv.mq.cloud.mq.DefaultInvoke;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;

/**
 * topic流量服务
 * 
 * @Description:
 * @author yongfeigao
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
        List<Topic> topicList = topicListResult.getResult();
        mqAdminTemplate.execute(new DefaultInvoke() {
            public void invoke(MQAdminExt mqAdmin) throws Exception {
                for (Topic topic : topicList) {
                    TopicTraffic topicTraffic = new TopicTraffic();
                    topicTraffic.setCreateTime(time);
                    topicTraffic.setClusterId(mqCluster().getId());
                    fetchTraffic(mqAdmin, topic.getName(), topic.getName(), topicTraffic);
                    if (topicTraffic.getCount() !=0 || topicTraffic.getSize() != 0) {
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
        return BrokerStatsManager.TOPIC_PUT_NUMS;
    }

    @Override
    protected String getSizeKey() {
        return BrokerStatsManager.TOPIC_PUT_SIZE;
    }

    @Override
    public Result<List<TopicTraffic>> query(long id, String date) {
        List<TopicTraffic> list = null;
        try {
            list = topicTrafficDao.select(id, date);
        } catch (Exception e) {
            logger.error("query traffic err, id:{},date:{}", id, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    public Result<TopicTraffic> queryTotalTraffic(long id, String date) {
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
    public Result<List<TopicTraffic>> query(List<Long> idList, String date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Result<List<TopicTraffic>> query(List<Long> idList, String date, String time) {
        List<TopicTraffic> list = null;
        try {
            list = topicTrafficDao.selectByIdListDateTime(idList, date, time);
        } catch (Exception e) {
            logger.error("query traffic err, idList:{},date:{},time:{}", idList, date, time, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }
    
    /**
     * 查询小于某一日期的所有流量
     * @param tid
     * @param createDate
     * @return
     */
    public Result<List<TopicTraffic>> queryRangeTraffic(long tid, String createDate) {
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
     * @param tid
     * @param createDate
     * @param createTimeList
     * @return
     */
    public Result<List<TopicTraffic>> queryRangeTraffic(long tid, String createDate, List<String> createTimeList) {
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
     * @param date
     * @param timeList
     * @return
     */
    public Result<List<TopicTraffic>> query(String date, List<String> timeList, List<Integer> clusterIdList) {
        List<TopicTraffic> list = null;
        try {
            list = topicTrafficDao.selectByDateTime(date, timeList, clusterIdList);
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
}
