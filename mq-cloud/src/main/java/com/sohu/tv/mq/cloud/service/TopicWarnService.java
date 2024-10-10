package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.bo.TopicWarnConfig;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import org.apache.rocketmq.common.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * topic预警配置服务
 *
 * @author yongfeigao
 * @date 2024年09月06日
 */
@Service
public class TopicWarnService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TopicWarnConfigService topicWarnConfigService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private TopicService topicService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserService userService;

    @Autowired
    private AlarmConfigBridingService alarmConfigBridingService;

    @Autowired
    private TopicTrafficService topicTrafficService;

    /**
     * 小时预警
     */
    public int warnHour(List<TopicTraffic> topicTraffics) {
        return warnHour(topicTraffics, null);
    }

    /**
     * 小时预警
     */
    public int warnHour(List<TopicTraffic> topicTraffics, Map<Long, List<TopicWarnConfig>> topicWarnConfigMap) {
        if (topicWarnConfigMap == null) {
            topicWarnConfigMap = getTopicWarnConfigMap(topicWarnConfigService::queryHourAll);
        }
        return warn(topicWarnConfigMap, (entry, topic) -> {
            long tid = entry.getKey();
            Optional<TopicTraffic> trafficOptional = topicTraffics.stream().filter(t -> t.getTid() == tid).findAny();
            if (!trafficOptional.isPresent()) {
                return null;
            }
            return toTopicWarnConfigs(entry.getValue(), topic, trafficOptional.get().getCount(), topic.getCount());
        });
    }

    /**
     * 天流量预警
     */
    public int warnDay() {
        return warnDay(null);
    }

    /**
     * 天流量预警
     */
    public int warnDay(Map<Long, List<TopicWarnConfig>> topicWarnConfigMap) {
        if (topicWarnConfigMap == null) {
            topicWarnConfigMap = getTopicWarnConfigMap(topicWarnConfigService::queryDayAll);
        }
        return warn(topicWarnConfigMap, (entry, topic) -> {
            long day1Count = topic.getCount1d();
            long day2Count = topic.getCount2d() - topic.getCount1d();
            return toTopicWarnConfigs(entry.getValue(), topic, day1Count, day2Count);
        });
    }

    /**
     * 5分钟流量预警
     */
    public int warn5Minute() {
        return warn5Minute(null);
    }

    /**
     * 5分钟流量预警
     */
    public int warn5Minute(Map<Long, List<TopicWarnConfig>> topicWarnConfigMap) {
        if (topicWarnConfigMap == null) {
            topicWarnConfigMap = getTopicWarnConfigMap(topicWarnConfigService::queryMinuteAll);
        }
        return warn(topicWarnConfigMap, (entry, topic) -> {
            Map<String, List<String>> timeMap = DateUtil.getBeforeTimes(10);
            Result<List<TopicTraffic>> topicTrafficResult = topicTrafficService.queryRangeTraffic(topic.getId(), timeMap);
            if (topicTrafficResult.getException() != null) {
                return null;
            }
            // 计算5分钟的流量
            Pair<Double, Double> pair = calculate5MinuteCount(topicTrafficResult.getResult(), timeMap);
            return toTopicWarnConfigs(entry.getValue(), topic, pair.getObject1(), pair.getObject2());
        });
    }

    /**
     * 计算5分钟的流量
     */
    private Pair<Double, Double> calculate5MinuteCount(List<TopicTraffic> topicTraffics, Map<String, List<String>> timeMap) {
        if (CollectionUtils.isEmpty(topicTraffics)) {
            return new Pair<>(0D, 0D);
        }
        double minute5Count = 0;
        double prevMinute5Count = 0;
        // 将流量数据转换为Map，方便查找
        Map<String, TopicTraffic> trafficMap = topicTraffics.stream()
                .collect(Collectors.toMap(TopicTraffic::getCreateTime, Function.identity()));
        List<String> times = toTimeList(timeMap);
        // 计算当前5分钟的流量和上一个5分钟的流量
        for (int i = 0; i < times.size(); ++i) {
            String time = times.get(i);
            TopicTraffic topicTraffic = trafficMap.get(time);
            if (topicTraffic != null) {
                if (i < 5) {
                    minute5Count += topicTraffic.getCount();
                } else {
                    prevMinute5Count += topicTraffic.getCount();
                }
            }
        }
        return new Pair<>(minute5Count, prevMinute5Count);
    }

    /**
     * 将timeMap转换为timeList，为了按照时间排序
     */
    public List<String> toTimeList(Map<String, List<String>> timeMap) {
        List<Pair<String, String>> timeList = new ArrayList<>();
        timeMap.forEach((k, v) -> v.forEach(t -> timeList.add(new Pair<>(k + t, t))));
        return timeList.stream()
                .sorted(Comparator.comparing(Pair::getObject1))
                .map(Pair::getObject2)
                .collect(Collectors.toList());
    }

    /**
     * 获取topic预警配置
     */
    public Map<Long, List<TopicWarnConfig>> getTopicWarnConfigMap(Supplier<Result<List<TopicWarnConfig>>> topicWarnConfigSupplier) {
        Result<List<TopicWarnConfig>> listResult = topicWarnConfigSupplier.get();
        if (listResult.isEmpty()) {
            return null;
        }
        Map<Long, List<TopicWarnConfig>> topicWarnConfigMap = new HashMap<>();
        listResult.getResult().stream()
                .filter(TopicWarnConfig::isInWarnTime) // 过滤掉不在报警时间的配置
                .collect(Collectors.groupingBy(TopicWarnConfig::getTid)) // 按照tid分组
                .forEach(topicWarnConfigMap::put);
        return topicWarnConfigMap;
    }

    /**
     * 预警
     */
    private int warn(Map<Long, List<TopicWarnConfig>> topicWarnConfigMap,
                     BiFunction<Entry<Long, List<TopicWarnConfig>>, Topic, List<TopicWarnConfig>> warnFunction) {
        if (topicWarnConfigMap == null || topicWarnConfigMap.isEmpty()) {
            return 0;
        }
        // 查询topic
        Result<List<Topic>> topicListResult = topicService.queryTopicList(topicWarnConfigMap.keySet());
        if (topicListResult.isEmpty()) {
            return 0;
        }
        List<Topic> topics = topicListResult.getResult();
        for (Entry<Long, List<TopicWarnConfig>> entry : topicWarnConfigMap.entrySet()) {
            long tid = entry.getKey();
            Optional<Topic> topicOptional = topics.stream().filter(t -> t.getId() == tid).findAny();
            if (!topicOptional.isPresent()) {
                continue;
            }
            // 发送报警
            try {
                sendAlert(warnFunction.apply(entry, topicOptional.get()), topicOptional.get());
            } catch (Exception e) {
                logger.error("warn error, topic:{}", topicOptional.get().getName(), e);
            }
        }
        return topics.size();
    }

    private List<TopicWarnConfig> toTopicWarnConfigs(Collection<TopicWarnConfig> configs, Topic topic, double value1, double value2) {
        List<TopicWarnConfig> warnResult = new ArrayList<>();
        for (TopicWarnConfig config : configs) {
            TopicWarnConfig result = config.chooseTopicWarnConfig(alarmConfigBridingService, topic.getName()).warn(value1, value2);
            if (result != null) {
                warnResult.add(result);
            }
        }
        return warnResult;
    }

    // 发送报警
    public void sendAlert(List<TopicWarnConfig> configs, Topic topic) {
        if (configs == null || configs.isEmpty()) {
            return;
        }
        Result<List<User>> userListResult = userService.queryProducerUserList(topic.getId());
        if (userListResult.isEmpty()) {
            return;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("topic", mqCloudConfigHelper.getTopicProduceLink(topic.getId(), topic.getName()));
        paramMap.put("list", configs);
        paramMap.put("resource", topic.getName());
        alertService.sendWarn(userListResult.getResult(), WarnType.TOPIC_WARN, paramMap);
    }
}
