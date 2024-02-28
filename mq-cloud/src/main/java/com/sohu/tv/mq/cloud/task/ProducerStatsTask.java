package com.sohu.tv.mq.cloud.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.bo.ProducerTotalStat;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.ProducerStatService;
import com.sohu.tv.mq.cloud.service.ProducerTotalStatService;
import com.sohu.tv.mq.cloud.service.UserProducerService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;

import net.javacrumbs.shedlock.core.SchedulerLock;

/**
 * producer统计任务
 * 
 * @author yongfeigao
 * @date 2018年6月26日
 */
public class ProducerStatsTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProducerTotalStatService producerTotalStatService;

    @Autowired
    private ProducerStatService producerStatService;

    @Autowired
    private UserProducerService userProducerService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private UserService userService;

    /**
     * 删除统计表数据
     */
    @Scheduled(cron = "0 03 5 * * ?")
    @SchedulerLock(name = "deleteProducerStats", lockAtMostFor = 600000, lockAtLeastFor = 59000)
    public void deleteProducerStats() {
        // 10天以前
        long now = System.currentTimeMillis();
        Date daysAgo = new Date(now - 10L * 24 * 60 * 60 * 1000);
        // 删除producerStat
        Result<Integer> result = producerStatService.delete(daysAgo);
        log(result, daysAgo, "producerStat", now);

        // 删除producerTotalStat
        now = System.currentTimeMillis();
        result = producerTotalStatService.delete(daysAgo);
        log(result, daysAgo, "producerTotalStat", now);
    }

    /**
     * 删除数据
     */
    private void log(Result<Integer> result, Date date, String flag, long start) {
        if (result.isOK()) {
            logger.info("{}:{}, delete success, rows:{} use:{}ms", flag, date,
                    result.getResult(), (System.currentTimeMillis() - start));
        } else {
            if (result.getException() != null) {
                logger.error("{}:{}, delete err", flag, date, result.getException());
            } else {
                logger.info("{}:{}, delete failed", flag, date);
            }
        }
    }

    /**
     * 5分钟异常上报
     */
    @Scheduled(cron = "7 */5 * * * ?")
    @SchedulerLock(name = "exceptionProducerStats", lockAtMostFor = 60000, lockAtLeastFor = 5900)
    public void exceptionProducerStats() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                Date date = new Date();
                String time = DateUtil.getFormat(DateUtil.HHMM).format(new Date(date.getTime() - 5 * 60 * 1000));
                producerException(DateUtil.format(date), time);
            }
        });
    }

    protected void producerException(int dt, String time) {
        long start = System.currentTimeMillis();
        int size = 0;
        Result<List<ProducerTotalStat>> listResult = producerTotalStatService.queryExceptionList(dt, time);
        if (listResult.isNotEmpty()) {
            List<ProducerTotalStat> list = listResult.getResult();
            size = list.size();
            // 按生产者分组
            Map<String, List<ProducerTotalStat>> groupedMap = group(list);
            for (String producer : groupedMap.keySet()) {
                List<ProducerTotalStat> totalList = groupedMap.get(producer);
                // 获取发送者列表
                List<UserProducer> userProducerList = getUserProducer(producer);
                if (userProducerList != null) {
                    // 验证是否忽略报警
                    if (mqCloudConfigHelper.isIgnoreErrorProducer(producer)) {
                        logger.info("ignore producer:{} error", producer);
                        continue;
                    }
                    List<User> users = getWarnUser(userProducerList);
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put("link", mqCloudConfigHelper.getTopicLink(userProducerList.get(0).getTid(), producer));
                    paramMap.put("list", totalList);
                    paramMap.put("resource", producer);
                    alertService.sendWarn(users, WarnType.PRODUCE_EXCEPTION, paramMap);
                } else {
                    logger.warn("can not get the relationship between user and producer！producer:{}", producer);
                }
            }
        }
        logger.info("exceptionProducerStats dt:{} time:{} size:{} use:{}ms", dt, time, size,
                (System.currentTimeMillis() - start));
    }

    /**
     * 获取topic id
     * 
     * @param producer
     * @return
     */
    private List<UserProducer> getUserProducer(String producer) {
        Result<List<UserProducer>> userProducerResult = userProducerService.queryUserProducer(producer);
        if (userProducerResult.isNotEmpty()) {
            return userProducerResult.getResult();
        }
        return null;
    }

    /**
     * 获取用户邮件地址
     * 
     * @return
     */
    private List<User> getWarnUser(List<UserProducer> userProducerList) {
        if (userProducerList == null) {
            return null;
        }
        Set<Long> userIDSet = new HashSet<Long>();
        for (UserProducer userProducer : userProducerList) {
            userIDSet.add(userProducer.getUid());
        }
        // 获取用户id
        if (!userIDSet.isEmpty()) {
            // 获取用户信息
            Result<List<User>> userListResult = userService.query(userIDSet);
            if (userListResult.isNotEmpty()) {
                return userListResult.getResult();
            }
        }
        return null;
    }

    /**
     * 按照producer分组
     * 
     * @param statlist
     * @return
     */
    private Map<String, List<ProducerTotalStat>> group(List<ProducerTotalStat> statlist) {
        Map<String, List<ProducerTotalStat>> groupedMap = new HashMap<String, List<ProducerTotalStat>>();
        for (ProducerTotalStat producerTotalStat : statlist) {
            List<ProducerTotalStat> list = groupedMap.get(producerTotalStat.getProducer());
            if (list == null) {
                list = new ArrayList<ProducerTotalStat>();
                groupedMap.put(producerTotalStat.getProducer(), list);
            }
            list.add(producerTotalStat);
        }
        return groupedMap;
    }
}
