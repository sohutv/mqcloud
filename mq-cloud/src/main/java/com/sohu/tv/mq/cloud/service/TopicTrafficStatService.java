package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.bo.TopicTrafficCheckResult;
import com.sohu.tv.mq.cloud.bo.TopicTrafficStat;
import com.sohu.tv.mq.cloud.bo.TopicTrafficWarnConfig;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.util.Jointer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.sohu.tv.mq.cloud.dao.TopicTrafficStatDao;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * @author yongweizhao
 * @create 2020/8/3 17:06
 */
@Service
public class TopicTrafficStatService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int ONE_MIN = 1 * 60 * 1000;

    private static final int FIVE_MIN_BEFORE = 5;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private AlertService alertService;

    @Autowired
    private TopicTrafficService topicTrafficService;

    @Autowired
    private TopicTrafficWarnConfigService topicTrafficWarnConfigService;

    @Autowired
    private UserService userService;

    @Autowired
    private TopicTrafficStatDao topicTrafficStatDao;

    /**
     * 保存topic统计流量
     * @param topicTrafficStat
     * @return
     */
    public Result<TopicTrafficStat> save(TopicTrafficStat topicTrafficStat) {
        try {
            topicTrafficStatDao.insertAndUpdate(topicTrafficStat);
        } catch (Exception e) {
            logger.error("insert err, topicTrafficStat:{}", topicTrafficStat, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicTrafficStat);
    }

    /**
     * 根据tid查询统计信息
     * @param tid
     * @return
     */
    public Result<TopicTrafficStat> query(long tid) {
        TopicTrafficStat topicTrafficStat = null;
        try {
            topicTrafficStat = topicTrafficStatDao.select(tid);
        } catch (Exception e) {
            logger.error("queryTopicTrafficStat err, tid:{}", tid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicTrafficStat);
    }

    /**
     * 查询所有的tid列表
     */
    public List<Long> queryAllTid() {
        List<Long> list = null;
        try {
            list = topicTrafficStatDao.selectAllTid();
        } catch (Exception e) {
            logger.error("queryAllTid err,", e);
        }
        return list;
    }

    /**
     * 删除统计信息
     */
    public Result<Integer> delete(List<Long> tidList) {
        Integer count;
        try {
            count = topicTrafficStatDao.delete(tidList);
        } catch (Exception e) {
            logger.error("del topicTrafficStat err, tidList:{}", StringUtils.join(tidList, ","), e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }

    /**
     * 获取所有开启了流量预警功能的topic
     */
    public List<Topic> queryTrafficWarnEnabledTopicList() {
        if (clusterService.getAllMQCluster() == null) {
            logger.warn("mqcluster is null");
            return null;
        }
        List<Topic> topicList = new ArrayList<>();
        for (Cluster mqCluster : clusterService.getAllMQCluster()) {
            Result<List<Topic>> topicListResult = topicService.queryTrafficWarnEnabledTopicList(mqCluster);
            if (topicListResult.isNotEmpty()) {
                topicList.addAll(topicListResult.getResult());
            }
        }
        return topicList;
    }

    /**
     * 流量统计分析
     */
    public void trafficStatAll() {
        List<Topic> topicList = queryTrafficWarnEnabledTopicList();
        if (CollectionUtils.isEmpty(topicList)) {
            return;
        }
        // 获取所有topic流量阈值配置
        Result<List<TopicTrafficWarnConfig>> configResult = topicTrafficWarnConfigService.queryAll();
        if (configResult.isEmpty()) {
            return;
        }
        Map<String, TopicTrafficWarnConfig> configMap = new HashMap<>();
        TopicTrafficWarnConfig defaultConfig = null;
        Iterator<TopicTrafficWarnConfig> iterator = configResult.getResult().iterator();
        while (iterator.hasNext()) {
            TopicTrafficWarnConfig config = iterator.next();
            if (StringUtils.isBlank(config.getTopic())) {
                defaultConfig = config;
            } else {
                configMap.put(config.getTopic(), config);
            }
        }
        // 获取当天时间
        String date = DateUtil.getFormatNow(DateUtil.YMD_DASH);
        for (Topic topic : topicList) {
            Result<List<TopicTraffic>> topicTrafficResult = topicTrafficService.queryRangeTraffic(topic.getId(), date);
            if (topicTrafficResult.isNotEmpty()) {
                TrafficStatCheckStrategy strategy = null;
                TopicTrafficWarnConfig strategyConfig = null;
                if (configMap.containsKey(topic.getName())) {
                    strategyConfig = configMap.get(topic.getName());
                    strategyConfig.copyProperties(defaultConfig);
                    strategy = new TrafficSimpleStatStrategy(topicTrafficResult.getResult(), strategyConfig);
                } else {
                    strategy = new TrafficSimpleStatStrategy(topicTrafficResult.getResult(), defaultConfig);
                }
                stat(strategy);
            }
        }
    }

    // 统计tid一天的流量
    private void stat(TrafficStatCheckStrategy strategy) {
        TopicTrafficStat topicTrafficStat = strategy.stat();
        if (topicTrafficStat != null) {
            logger.info("topic traffic stat:{}", topicTrafficStat.toString());
            save(topicTrafficStat);
        }
    }

    /**
     * 流量监测
     */
    public void check(List<Topic> topicList) {
        // 1. 获取topic流量阈值配置
        Result<List<TopicTrafficWarnConfig>> configResult = topicTrafficWarnConfigService.queryAll();
        if (configResult.isEmpty()) {
            return;
        }
        Map<String, TopicTrafficWarnConfig> configMap = new HashMap<>();
        TopicTrafficWarnConfig defaultConfig = null;
        Iterator<TopicTrafficWarnConfig> iterator = configResult.getResult().iterator();
        while (iterator.hasNext()) {
            TopicTrafficWarnConfig config = iterator.next();
            if (StringUtils.isBlank(config.getTopic())) {
                defaultConfig = config;
            } else {
                configMap.put(config.getTopic(), config);
            }
        }
        // 当天时间
        String date = DateUtil.getFormatNow(DateUtil.YMD_DASH);
        // 5分钟前时间间隔
        List<String> timeList = getBeforeTimes(FIVE_MIN_BEFORE);
        for (Topic topic : topicList) {
            // 2. 获取前5分钟topic流量列表
            long tid = topic.getId();
            Result<List<TopicTraffic>> topicTrafficResult = topicTrafficService.queryRangeTraffic(tid, date, timeList);
            if (topicTrafficResult.isEmpty()) {
                continue;
            }
            // 3. 获取统计结果
            Result<TopicTrafficStat> topicTrafficStatResult = query(tid);
            if (topicTrafficStatResult.isNotOK()) {
                continue;
            }
            // 4. 构建strategy
            TopicTrafficWarnConfig config = null;
            if (configMap.containsKey(topic.getName())) {
                config = configMap.get(topic.getName());
                config.copyProperties(defaultConfig);
            } else {
                config = defaultConfig;
            }
            TrafficStatCheckStrategy strategy = new TrafficSimpleStatStrategy(topicTrafficStatResult.getResult(), config);
            // 5. 检测
            if (config.isAlert()) {
                List<TopicTrafficCheckResult> checkResult = strategy.check(topicTrafficResult.getResult());
                // 6. 告警
                sendAlert(checkResult, config, topic);
            }
        }
    }

    /**
     * 获取beforeTime前到当前时间之间的时间集合,格式HHMM
     */
    private List<String> getBeforeTimes(int beforeTime) {
        Date now = new Date();
        // 计算前beforeTime分钟间隔
        List<String> timeList = new ArrayList<String>();
        Date begin = new Date(now.getTime() - beforeTime * ONE_MIN + 30);
        while (begin.before(now)) {
            String time = DateUtil.getFormat(DateUtil.HHMM).format(begin);
            timeList.add(time);
            begin.setTime(begin.getTime() + ONE_MIN);
        }
        return timeList;
    }

    // 发送报警
    public void sendAlert(List<TopicTrafficCheckResult> checkResultList, TopicTrafficWarnConfig config, Topic topic) {
        if (CollectionUtils.isEmpty(checkResultList) || !config.isAlert()) {
            return;
        }
        StringBuilder content = new StringBuilder("详细如下:<br><br>");
        content.append("topic: <b>");
        content.append(mqCloudConfigHelper.getTopicProduceLink(topic.getId(), topic.getName()));
        content.append(" 检测到流量异常: <br>");
        content.append("<table border=1>");
        content.append("<thead>");
        content.append("<tr>");
        content.append("<th>时间</th>");
        content.append("<th>详情</th>");
        content.append("</tr>");
        content.append("</thead>");
        content.append("<tbody>");
        for (TopicTrafficCheckResult checkResult : checkResultList) {
            String warnTime = checkResult.getTime();
            String warnInfo = checkResult.getWarnInfo();
            content.append("<tr>");
            content.append("<td>");
            content.append(warnTime);
            content.append("</td>");
            content.append("<td>");
            content.append(warnInfo);
            content.append("</td>");
            content.append("</tr>");
        }
        content.append("</tbody>");
        content.append("</table>");
        // 根据配置发送给不同的报警接收人
        String email = getAlarmReceiverEmails(config.getAlarmReceiver(), topic.getId());
        alertService.sendWarnMail(email, "topic流量预警", content.toString());
    }

    /**
     * 获取报警人
     */
    private String getAlarmReceiverEmails(int alarmType, long topicId) {
        Set<User> userSet = new HashSet<>();
        Result<List<User>> producerUserListResult = null;
        Result<List<User>> consumerUserListResult = null;
        switch (alarmType) {
            case 0:
                producerUserListResult = userService.queryProducerUserList(topicId);
                consumerUserListResult = userService.queryConsumerUserList(topicId);
                break;
            case 1:
                producerUserListResult = userService.queryProducerUserList(topicId);
                break;
            case 2:
                consumerUserListResult = userService.queryConsumerUserList(topicId);
                break;
            case 3:
                break;
            default:
        }
        if (producerUserListResult != null && producerUserListResult.isNotEmpty()) {
            userSet.addAll(producerUserListResult.getResult());
        }
        if (consumerUserListResult != null && consumerUserListResult.isNotEmpty()) {
            userSet.addAll(consumerUserListResult.getResult());
        }
        String email = Jointer.BY_COMMA.join(userSet, u -> u.getEmail());
        return email;
    }
}
