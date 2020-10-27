package com.sohu.tv.mq.cloud.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.common.model.BrokerStoreStat;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.BrokerStoreStatService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;

import net.javacrumbs.shedlock.core.SchedulerLock;

/**
 * broker存储统计任务
 * 
 * @author yongfeigao
 * @date 2020年4月26日
 */
public class BrokerStoreStatTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int ONE_MIN = 1 * 60 * 1000;

    @Autowired
    private BrokerStoreStatService brokerStoreStatService;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private AlertService alertService;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    /**
     * broker store 流量收集
     */
    @Scheduled(cron = "3 */1 * * * *")
    @SchedulerLock(name = "collectBrokerStoreStatTraffic", lockAtMostFor = ONE_MIN, lockAtLeastFor = 59000)
    public void collectTraffic() {
        taskExecutor.execute(new Runnable() {
            public void run() {
                Result<List<Broker>> brokerListResult = brokerService.queryAll();
                if (brokerListResult.isEmpty()) {
                    logger.warn("brokerListResult is empty");
                    return;
                }
                long start = System.currentTimeMillis();
                int size = fetchAndSaveStoreStat(brokerListResult.getResult());
                logger.info("fetch and save store stat size:{}, use:{}ms", size, System.currentTimeMillis() - start);
            }
        });
    }

    private int fetchAndSaveStoreStat(List<Broker> brokerList) {
        List<BrokerStoreStat> brokerStoreStatList = new ArrayList<BrokerStoreStat>(brokerList.size());
        for (Broker broker : brokerList) {
            // 非master跳过
            if (broker.getBrokerID() != 0) {
                continue;
            }
            Cluster cluster = clusterService.getMQClusterById(broker.getCid());
            if (cluster == null) {
                logger.warn("cid:{} is no cluster", broker.getCid());
                continue;
            }
            // 流量抓取
            Result<BrokerStoreStat> result = brokerStoreStatService.fetchBrokerStoreStat(cluster,
                    broker.getAddr());
            BrokerStoreStat brokerStoreStat = result.getResult();
            if (brokerStoreStat == null) {
                logger.warn("broker:{} stat is null, msg:{}", broker.getAddr(),
                        result.getException() != null ? result.getException().getMessage() : "");
                continue;
            }
            // 数据组装
            Date now = new Date();
            brokerStoreStat.setCreateDate(NumberUtils.toInt(DateUtil.formatYMD(now)));
            brokerStoreStat.setCreateTime(DateUtil.getFormat(DateUtil.HHMM).format(now));
            brokerStoreStat.setBrokerIp(broker.getIp());
            brokerStoreStat.setClusterId(cluster.getId());
            // 数据存储
            brokerStoreStatService.save(brokerStoreStat);
            brokerStoreStatList.add(brokerStoreStat);
        }
        // 预警
        warn(brokerStoreStatList);
        return brokerStoreStatList.size();
    }

    /**
     * 预警
     * 
     * @param brokerStoreStatList
     */
    public void warn(List<BrokerStoreStat> brokerStoreStatList) {
        StringBuilder content = new StringBuilder();
        for (BrokerStoreStat brokerStoreStat : brokerStoreStatList) {
            if (brokerStoreStat.getMax() < 500 && brokerStoreStat.getPercent99() < 400) {
                continue;
            }
            content.append("<tr>");
            content.append("<td>");
            content.append(clusterService.getMQClusterById(brokerStoreStat.getClusterId()).getName());
            content.append("</td>");
            content.append("<td>");
            content.append(mqCloudConfigHelper.getBrokerStoreLink(brokerStoreStat.getClusterId(),
                    brokerStoreStat.getBrokerIp()));
            content.append("</td>");
            content.append("<td>");
            content.append(brokerStoreStat.getAvg());
            content.append("ms</td>");
            content.append("<td>");
            content.append(brokerStoreStat.getPercent90());
            content.append("ms</td>");
            content.append("<td>");
            content.append(brokerStoreStat.getPercent99());
            content.append("ms</td>");
            content.append("<td>");
            content.append(brokerStoreStat.getMax());
            content.append("ms</td>");
            content.append("<td>");
            content.append(brokerStoreStat.getCount());
            content.append("</td>");
            content.append("</tr>");
        }
        if (content.length() <= 0) {
            return;
        }
        String header = "<table border=1><thead><tr><th>集群</th><th>broker</th><th>avg</th><th>90%</th><th>99%</th>"
                + "<th>max</th><th>写入量</th></tr></thead><tbody>";
        String footer = "</tbody></table>";
        alertService.sendWarnMail(null, "BrokerStore", header + content.toString() + footer);
    }

    /**
     * 删除统计表数据
     */
    @Scheduled(cron = "0 45 4 * * ?")
    @SchedulerLock(name = "deleteBrokerStoreStat", lockAtMostFor = 600000, lockAtLeastFor = 59000)
    public void deleteProducerStats() {
        // 10天以前
        long now = System.currentTimeMillis();
        Date daysAgo = new Date(now - 10L * 24 * 60 * 60 * 1000);
        // 删除producerStat
        Result<Integer> result = brokerStoreStatService.delete(daysAgo);
        log(result, daysAgo, "brokerStoreStat", now);
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
}
