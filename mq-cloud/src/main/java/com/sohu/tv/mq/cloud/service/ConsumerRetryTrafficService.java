package com.sohu.tv.mq.cloud.service;

import java.util.Date;
import java.util.List;

import org.apache.rocketmq.store.stats.BrokerStatsManager;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.TopicConsumer;
import com.sohu.tv.mq.cloud.bo.TopicHourTraffic;
import com.sohu.tv.mq.cloud.bo.Traffic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.mq.DefaultInvoke;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * topic流量服务
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月26日
 */
@Service
public class ConsumerRetryTrafficService extends TrafficService<Traffic> {
    @Autowired
    private TopicService topicService;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private UserConsumerService userConsumerService;
    
    @Autowired
    private AlarmConfigBridingService alarmConfigBridingService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @Autowired
    private ClusterService clusterService;
    
    /**
     * 收集重试流量
     * 
     * @param mqCluster
     * @return topic size
     */
    public int collectHourRetryTraffic() {
        Result<List<TopicConsumer>> topicConsumerListResult = topicService.queryTopicConsumer();
        if (topicConsumerListResult.isEmpty()) {
            logger.error("cannot get TopicConsumer");
            return 0;
        }
        List<TopicConsumer> topicConsumerList = topicConsumerListResult.getResult();
        for (TopicConsumer topicConsumer : topicConsumerList) {
            mqAdminTemplate.execute(new DefaultInvoke() {
                public void invoke(MQAdminExt mqAdmin) throws Exception {
                    String statKey = topicConsumer.getTopic() + "@" + topicConsumer.getConsumer();
                    TopicHourTraffic topicTraffic = new TopicHourTraffic();
                    fetchTraffic(mqAdmin, topicConsumer.getTopic(), statKey, topicTraffic);
                    if(topicTraffic.getCount() > 0) { 
                        Result<User> userResult = userConsumerService.queryUserByConsumer(topicConsumer.getTid(), 
                                topicConsumer.getCid());
                        String email = null;
                        if(userResult.isOK()) {
                            email = userResult.getResult().getEmail();
                        }
                        long consumerFailCount = alarmConfigBridingService
                                .getConsumerFailCount(userResult.getResult().getId(), topicConsumer.getTopic());
                        if (consumerFailCount > 0 && consumerFailCount < topicTraffic.getCount()) {
                            // 验证报警频率
                            if (alarmConfigBridingService.needWarn("consumerFail", topicConsumer.getTopic(), topicConsumer.getConsumer())) {
                                alertService.sendWanMail(email, "消费失败",
                                        "topic:<b>" + topicConsumer.getTopic() + "</b> 消费者:<b>"
                                                + mqCloudConfigHelper.getTopicConsumeLink(topicConsumer.getTid(), topicConsumer.getConsumer())
                                                + "</b> 消费失败量:" + topicTraffic.getCount());
                            }         
                        }
                        // 打印报警信息
                        logger.warn("alert! consumer fail topic:{}, consumer:{}, consumerFailCount:{}",
                                topicConsumer.getTopic(), topicConsumer.getConsumer(), topicTraffic.getCount());
                    }
                }
                public Cluster mqCluster() {
                    return clusterService.getMQClusterById(topicConsumer.getClusterId());
                }
            });
        }
        return topicConsumerList.size();
    }
    
    public Result<Integer> delete(Date date) {
        throw new UnsupportedOperationException();
    }

    protected String getCountKey() {
        return BrokerStatsManager.SNDBCK_PUT_NUMS;
    }

    protected String getSizeKey() {
        return null;
    }

    public Result<List<Traffic>> query(long id, String date) {
        throw new UnsupportedOperationException();
    }
    
    public Result<List<Traffic>> query(List<Long> idList, String date) {
        throw new UnsupportedOperationException();
    }

    public Result<List<Traffic>> query(List<Long> idList, String date, String time) {
        throw new UnsupportedOperationException();
    }
}
