package com.sohu.tv.mq.cloud.task;

import java.util.List;
import java.util.Properties;

import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.mq.DefaultInvoke;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.util.Result;
/**
 * 集群信息更新
 * 
 * @author yongfeigao
 * @date 2019年9月23日
 */
@Component
public class ClusterInfoUpdateTask {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private BrokerService brokerService;
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    @Scheduled(cron = "4 30 3 * * ?")
    public void updateClusterInfo() {
        if(clusterService.getAllMQCluster() == null) {
            return;
        }
        logger.info("update cluster info start");
        long start = System.currentTimeMillis();
        for (Cluster mqCluster : clusterService.getAllMQCluster()) {
            Result<List<Broker>> brokerListResult = brokerService.query(mqCluster.getId());
            if (brokerListResult.isEmpty()) {
                return;
            }
            mqAdminTemplate.execute(new DefaultInvoke() {
                public void invoke(MQAdminExt mqAdmin) throws Exception {
                    List<Broker> brokerList = brokerListResult.getResult();
                    for (Broker broker : brokerList) {
                        try {
                            Properties properties = mqAdmin.getBrokerConfig(broker.getAddr());
                            clusterService.updateFileReservedTime(properties, mqCluster.getId());
                        } catch (Exception e) {
                            logger.warn("fetch broker:{} config", broker.getAddr(), e);
                        }
                    }
                }

                public Cluster mqCluster() {
                    return mqCluster;
                }
            });
        }
        logger.info("update cluster info end! use:{}ms", System.currentTimeMillis() - start);
    }

}
