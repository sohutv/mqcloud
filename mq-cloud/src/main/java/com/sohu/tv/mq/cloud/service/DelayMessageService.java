package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.dao.DelayMessageDao;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * 延迟消息服务
 * 
 * @Description:
 * @author zhehongyuan
 * @date 2019年4月23日
 */
@Service
public class DelayMessageService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DelayMessageDao delayMessageDao;

    /**
     * 延迟消息的流量使用客户端上报的数据
     * 
     * @param tid
     * @param createDate
     */
    public Result<List<TopicTraffic>> selectDelayMessageTraffic(long tid, int createDate) {
        List<TopicTraffic> topicTrafficList = null;
        try {
            topicTrafficList = delayMessageDao.selectTopicTraffic(tid, createDate);
        } catch (Exception e) {
            logger.error("selectDelayMessageTraffic err, tid:{}, createDate:{}", tid, createDate, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicTrafficList);
    }

    /**
     * 延迟消息的流量使用客户端上报的数据
     * 
     * @param tid
     * @param date
     * @param time
     * @return
     */
    public Result<TopicTraffic> query(long tid, int date, String time) {
        TopicTraffic topicTraffic = null;
        try {
            topicTraffic = delayMessageDao.selectByIdListDateTime(tid, date, time);
        } catch (Exception e) {
            logger.error("query traffic err, tid:{},date:{},time:{}", tid, date, time, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicTraffic);
    }
    
    /**
     * idList中的id需要全部属于延迟消息的topic，外部做过滤
     * 
     * @param idList
     * @param date
     * @param time
     * @return
     */
    public Result<List<TopicTraffic>> query(List<Long> idList, int date, String time) {
        if (idList == null || idList.isEmpty()) {
            return Result.getResult(null);
        }
        List<TopicTraffic> list = new ArrayList<TopicTraffic>(idList.size());
        for (Long tid : idList) {
            Result<TopicTraffic> topicTraffic = query(tid, date, time);
            if (topicTraffic.isOK()) {
                list.add(topicTraffic.getResult());
            }
        }
        return Result.getResult(list);
    }
    
    /**
     * 查询延迟消息日流量
     * 
     * @param tid
     * @param date
     * @return
     */
    public Result<TopicTraffic> query(long tid, int date) {
        TopicTraffic topicTraffic = null;
        try {
            topicTraffic = delayMessageDao.selectTotalTraffic(tid, date);
        } catch (Exception e) {
            logger.error("query traffic err, tid:{},date:{}", tid, date, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicTraffic);
    }
}
