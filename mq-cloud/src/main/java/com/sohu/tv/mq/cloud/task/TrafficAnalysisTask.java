package com.sohu.tv.mq.cloud.task;

import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.service.TopicTrafficStatService;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 流量分析定时任务
 * @author yongweizhao
 * @create 2020/8/3 16:43
 */
public class TrafficAnalysisTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TopicTrafficStatService topicTrafficStatService;

    @Autowired
    private TaskExecutor taskExecutor;

    /**
     * 每天凌晨1点执行流量分析任务
     */
    @Scheduled(cron = "0 03 1 * * ?")
    @SchedulerLock(name = "topicTrafficStatAllTask", lockAtMostFor = 120000, lockAtLeastFor = 12000)
    public void topicTrafficStatAllTask() {
        long start = System.currentTimeMillis();
        topicTrafficStatService.trafficStatAll();
        logger.info("topicTrafficStatAllTask, use:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * 每隔10分钟检测是否有topic流量预警开关发生变化,有变化则重新统计
     */
    @Scheduled(cron = "2 */10 * * * ?")
    @SchedulerLock(name = "topicTrafficWarnStatusCheckTask", lockAtMostFor = 120000, lockAtLeastFor = 12000)
    public void topicTrafficWarnStatusCheckTask() {
        long start = System.currentTimeMillis();
        // 获取开启了流量预警的topic列表
        List<Topic> topicList = topicTrafficStatService.queryTrafficWarnEnabledTopicList();
        // 获取所有已有统计分析数据的tid
        List<Long> trafficStatTidList = topicTrafficStatService.queryAllTid();
        // 检测是否需要重新统计
        boolean needReStat = topicListOfTrafficWarnEnabledIsChange(trafficStatTidList, topicList);
        if (needReStat) {
            // 获取失效的统计tid列表
            List<Long> invalidTrafficStatTidList = invalidTrafficStatTidList(trafficStatTidList, topicList);
            // 删除
            if (!CollectionUtils.isEmpty(invalidTrafficStatTidList)) {
                topicTrafficStatService.delete(invalidTrafficStatTidList);
            }
            // 重新统计
            topicTrafficStatService.trafficStatAll();
            logger.info("topicTrafficWarnStatusCheckTask, use:{}ms", System.currentTimeMillis() - start);
        }
    }

    /**
     * topic流量监控分析,每隔5分钟检测流量变化
     */
    @Scheduled(cron = "3 */5 * * * ?")
    @SchedulerLock(name = "topicTrafficWarningTask", lockAtMostFor = 120000, lockAtLeastFor = 12000)
    public void topicTrafficWarningTask() {
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                List<Topic> topicList = topicTrafficStatService.queryTrafficWarnEnabledTopicList();
                if (CollectionUtils.isEmpty(topicList)) {
                    return;
                }
                topicTrafficStatService.check(topicList);
                logger.info("topicTrafficWarningTask, use:{}ms", System.currentTimeMillis() - start);
            }
        });
    }

    /**
     * 对比tid列表是否完全相同,不相等返回true,相等返回false
     */
    private boolean topicListOfTrafficWarnEnabledIsChange(List<Long> trafficStatTidList, List<Topic> topicList) {
        if (CollectionUtils.isEmpty(trafficStatTidList) || CollectionUtils.isEmpty(topicList)) {
            return true;
        }
        if (trafficStatTidList.size() != topicList.size()) {
            return true;
        }
        for (Topic topic : topicList) {
            if (!trafficStatTidList.contains(topic.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取在trafficStatTidList中但是不在topicList中的元素列表
     * 主要目的是找出关闭预警功能的部分予以删除
     */
    private List<Long> invalidTrafficStatTidList(List<Long> trafficStatTidList, List<Topic> topicList) {
        if (CollectionUtils.isEmpty(trafficStatTidList)) {
            return null;
        }
        if (CollectionUtils.isEmpty(topicList)) {
            return trafficStatTidList;
        }
        List<Long> topicIdList = new ArrayList<>();
        for (Topic topic : topicList) {
            topicIdList.add(topic.getId());
        }
        trafficStatTidList.retainAll(topicIdList);
        return trafficStatTidList;
    }
}
