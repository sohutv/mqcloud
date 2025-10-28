package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 小时流量服务
 * 
 * @author yongfeigao
 * @date 2020年7月9日
 */
public abstract class HourTrafficService extends TrafficService<Traffic> {

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserConsumerService userConsumerService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    /**
     * 收集流量
     * 
     * @param mqCluster
     * @return topic size
     */
    public int collectHourTraffic() {
        Result<List<TopicConsumer>> topicConsumerListResult = topicService.queryTopicConsumer(Consumer.CLUSTERING);
        if (topicConsumerListResult.isEmpty()) {
            logger.error("cannot get TopicConsumer");
            return 0;
        }
        int consumerSize = 0;
        List<TopicConsumer> topicConsumerList = topicConsumerListResult.getResult();
        for (TopicConsumer topicConsumer : topicConsumerList) {
            Cluster cluster = clusterService.getMQClusterById(topicConsumer.getClusterId());
            // 测试环境，监控所有的集群；online环境，只监控online集群
            if (!mqCloudConfigHelper.needMonitor(cluster.online())) {
                continue;
            }
            String statKey = topicConsumer.getTopic() + "@" + topicConsumer.getConsumer();
            TopicHourTraffic topicTraffic = new TopicHourTraffic();
            fetchTraffic(cluster, topicConsumer.getTopic(), statKey, topicTraffic);
            if (topicTraffic.getCount() > 0) {
                Result<List<User>> userListResult = userConsumerService.queryUserByConsumer(
                        topicConsumer.getTid(), topicConsumer.getCid());
                alert(topicTraffic, topicConsumer, userListResult.getResult());
                // 打印报警信息
                logger.warn("alert! consumer fail topic:{}, consumer:{}, consumerFailCount:{}",
                        topicConsumer.getTopic(), topicConsumer.getConsumer(), topicTraffic.getCount());
            }
            ++consumerSize;
        }
        return consumerSize;
    }
    
    protected abstract void alert(TopicHourTraffic topicTraffic, TopicConsumer topicConsumer, List<User> userList);

    public Result<Integer> delete(Date date) {
        throw new UnsupportedOperationException();
    }

    protected String getSizeKey() {
        return null;
    }

    public Result<List<Traffic>> query(long id, Date date) {
        throw new UnsupportedOperationException();
    }

    public Result<List<Traffic>> query(Collection<Long> idList, Date date) {
        throw new UnsupportedOperationException();
    }

    public Result<List<Traffic>> query(List<Long> idList, Date date, String time) {
        throw new UnsupportedOperationException();
    }
}