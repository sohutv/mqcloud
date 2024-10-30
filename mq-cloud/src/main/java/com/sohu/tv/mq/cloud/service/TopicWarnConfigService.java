package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicWarnConfig;
import com.sohu.tv.mq.cloud.dao.TopicWarnConfigDao;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.TopicWarnConfigVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sohu.tv.mq.cloud.bo.TopicWarnConfig.OperandType.*;

/**
 * topic预警配置服务
 *
 * @author yongfeigao
 * @date 2024年09月06日
 */
@Service
public class TopicWarnConfigService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TopicWarnConfigDao topicWarnConfigDao;

    @Autowired
    private TopicService topicService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    /**
     * 保存
     */
    public Result<Integer> save(TopicWarnConfig config) {
        try {
            return Result.getResult(topicWarnConfigDao.insert(config));
        } catch (Exception e) {
            logger.error("insert err, config:{}", config, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 更新
     */
    public Result<Integer> updateEnabled(long id, int enabled) {
        try {
            return Result.getResult(topicWarnConfigDao.updateEnabled(id, enabled));
        } catch (Exception e) {
            logger.error("updateEnabled err, id:{}, enabled:{}", id, enabled, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询
     */
    public Result<TopicWarnConfig> queryById(long id) {
        try {
            return Result.getResult(topicWarnConfigDao.selectById(id));
        } catch (Exception e) {
            logger.error("queryById err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 删除
     */
    public Result<Integer> delete(long id) {
        try {
            return Result.getResult(topicWarnConfigDao.delete(id));
        } catch (Exception e) {
            logger.error("delete err, id:{}", id, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询
     */
    public Result<List<TopicWarnConfig>> queryByTid(long tid) {
        try {
            return Result.getResult(topicWarnConfigDao.selectByTid(tid));
        } catch (Exception e) {
            logger.error("query err, tid:{}", tid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询分钟条件所有记录
     */
    public Result<List<TopicWarnConfig>> queryMinuteAll() {
        try {
            return Result.getResult(topicWarnConfigDao.selectByOperandType(MINUTE_5_LIST));
        } catch (Exception e) {
            logger.error("queryMinuteAll err", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询小时条件所有记录
     */
    public Result<List<TopicWarnConfig>> queryHourAll() {
        try {
            return Result.getResult(topicWarnConfigDao.selectByOperandType(HOUR_LIST));
        } catch (Exception e) {
            logger.error("queryHourAll err", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询天条件所有记录
     */
    public Result<List<TopicWarnConfig>> queryDayAll() {
        try {
            return Result.getResult(topicWarnConfigDao.selectByOperandType(DAY_LIST));
        } catch (Exception e) {
            logger.error("queryDayAll err", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询所有记录
     */
    public Result<?> queryAll() {
        try {
            // 查询所有配置
            List<TopicWarnConfig> topicWarnConfigs = topicWarnConfigDao.selectAll();
            if (topicWarnConfigs == null || topicWarnConfigs.isEmpty()) {
                return Result.getResult(Status.NO_RESULT);
            }
            // 按照tid分组
            Map<Long, TopicWarnConfigVO> map = new HashMap<>();
            topicWarnConfigs.forEach(topicWarnConfig -> {
                map.computeIfAbsent(topicWarnConfig.getTid(), id -> {
                    return new TopicWarnConfigVO();
                }).addTopicWarnConfig(topicWarnConfig);
            });
            // 查询topic
            Result<List<Topic>> topicsResult = topicService.queryTopicList(map.keySet());
            if (topicsResult.isEmpty()) {
                return topicsResult;
            }
            // 设置topic link
            topicsResult.getResult().forEach(topic -> {
                TopicWarnConfigVO topicWarnConfigVO = map.get(topic.getId());
                if (topicWarnConfigVO != null) {
                    topicWarnConfigVO.setTopic(topic);
                }
            });
            // 过滤掉没有topic的
            List<TopicWarnConfigVO> list = map.values().stream().filter(config -> config.getTopic() != null)
                    .sorted(Comparator.comparingLong(config -> config.getTopic().getId()))
                    .collect(Collectors.toList());
            return Result.getResult(list);
        } catch (Exception e) {
            logger.error("queryAll err", e);
            return Result.getDBErrorResult(e);
        }
    }
}
