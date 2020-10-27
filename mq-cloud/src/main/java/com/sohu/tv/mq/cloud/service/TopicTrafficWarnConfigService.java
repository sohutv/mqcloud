package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.TopicTrafficWarnConfig;
import com.sohu.tv.mq.cloud.dao.TopicTrafficWarnConfigDao;
import com.sohu.tv.mq.cloud.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yongweizhao
 * @create 2020/9/22 10:19
 */
@Service
public class TopicTrafficWarnConfigService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TopicTrafficWarnConfigDao topicTrafficWarnConfigDao;

    /**
     * 保存topic流量报警配置信息
     * @param topicTrafficWarnConfig
     * @return
     */
    public Result<Integer> save(TopicTrafficWarnConfig topicTrafficWarnConfig) {
        Integer count;
        try {
            count = topicTrafficWarnConfigDao.insertAndUpdate(topicTrafficWarnConfig);
        } catch (Exception e) {
            logger.error("insert err, topicTrafficWarnConfig:{}", topicTrafficWarnConfig, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 根据topicName查询配置信息
     * @param topicName
     * @return
     */
    public Result<TopicTrafficWarnConfig> query(String topicName) {
        TopicTrafficWarnConfig topicTrafficWarnConfig = null;
        try {
            topicTrafficWarnConfig = topicTrafficWarnConfigDao.selectByTopicName(topicName);
        } catch (Exception e) {
            logger.error("query topicTrafficWarnConfig err, topicName:{}", topicName, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicTrafficWarnConfig);
    }

    /**
     * 查询所有配置
     * @return
     */
    public Result<List<TopicTrafficWarnConfig>> queryAll() {
        List<TopicTrafficWarnConfig> list = null;
        try {
            list = topicTrafficWarnConfigDao.selectAll();
        } catch (Exception e) {
            logger.error("queryAll topicTrafficWarnConfig err,", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 删除
     * @param topicName
     * @return
     */
    public Result<Integer> delete(String topicName) {
        Integer res = null;
        try {
            res = topicTrafficWarnConfigDao.delete(topicName);
        } catch (Exception e) {
            logger.error("delete topicTrafficWarnConfig err, topicName:{}", topicName, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(res);
    }
}
