package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.bo.ClusterCapacity.BrokerCapacity;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.ClusterCapacityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 集群容量服务
 *
 * @author yongfeigao
 * @date 2024年5月29日
 */
@Service
public class ClusterCapacityService {

    @Autowired
    private ServerDataService serverDataService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private TopicService topicService;

    @Autowired
    private AlertService alertService;

    public Result<ClusterCapacityVO> getClusterCapacity() {
        // 获取所有服务器的容量
        Date yesterday = new Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000);
        List<ServerInfoExt> serverStatList = serverDataService.queryAllServer(yesterday);
        if (serverStatList.size() == 0) {
            return Result.getResult(Status.NO_RESULT);
        }
        // 按照集群维度组装数据
        Cluster[] clusters = clusterService.getAllMQCluster();
        if (clusters == null || clusters.length == 0) {
            return Result.getResult(Status.NO_RESULT);
        }
        Result<List<Broker>> brokerListResult = brokerService.queryAll();
        if (brokerListResult.isEmpty()) {
            return Result.getResult(Status.NO_RESULT);
        }
        List<Broker> brokerList = brokerListResult.getResult();
        setSlaveDaySize(brokerList);
        ClusterCapacityVO clusterCapacityVO = new ClusterCapacityVO();
        for (Cluster cluster : clusters) {
            // 获取所有topic的容量
            Result<List<Topic>> topicListResult = topicService.queryTopNSizeTopicList(cluster);
            if (topicListResult.isEmpty()) {
                continue;
            }
            ClusterCapacity clusterCapacity = new ClusterCapacity();
            clusterCapacity.setCluster(cluster);
            clusterCapacity.setTopicList(topicListResult.getResult());
            addLink(clusterCapacity.getTopicList());
            clusterCapacityVO.addClusterDataCapacity(clusterCapacity);
            // 组装broker数据
            for (Broker broker : brokerList) {
                if (broker.getCid() == cluster.getId()) {
                    // 组装服务器数据
                    for (ServerInfoExt serverStat : serverStatList) {
                        if (serverStat.getIp().equals(broker.getIp())) {
                            clusterCapacity.addBrokerCapacity(new BrokerCapacity(broker, serverStat));
                            break;
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(clusterCapacityVO.getClusterCapacityList())) {
            return Result.getResult(Status.NO_RESULT);
        }
        // 排序
        clusterCapacityVO.sortClusterDataCapacity();
        clusterCapacityVO.setClusterCapacityLink(mqCloudConfigHelper.getClusterCapacityLink());
        return Result.getResult(clusterCapacityVO);
    }

    private void addLink(List<Topic> topicList) {
        for (Topic topic : topicList) {
            topic.setInfo(mqCloudConfigHelper.getTopicLink(topic.getId()));
        }
    }

    /**
     * slave没有数据，设置为master的数据
     *
     * @param brokerList
     */
    private void setSlaveDaySize(List<Broker> brokerList) {
        for (Broker slave : brokerList) {
            if (slave.isMaster()) {
                continue;
            }
            for (Broker master : brokerList) {
                if (!master.isMaster()) {
                    continue;
                }
                if (slave.getBrokerName().equals(master.getBrokerName())) {
                    slave.setSize(master);
                    break;
                }
            }
        }
    }

    /**
     * 发送容量日报
     *
     * @return
     */
    public Result<?> sendCapacityDailyMail() {
        Result<ClusterCapacityVO> result = getClusterCapacity();
        if (result.isNotOK()) {
            return result;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("clusterCapacityVO", result.getResult());
        alertService.sendWarn(null, WarnType.CAPACITY_REPORT, paramMap);
        return Result.getOKResult();
    }
}
