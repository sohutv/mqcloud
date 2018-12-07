package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;


/**
 * broker
 * 
 * @author yongfeigao
 * @date 2018年11月14日
 */
@RestController
@RequestMapping("/admin/broker")
public class AdminBrokerController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private BrokerService brokerService;
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    /**
     * 刷新
     * 
     * @param cid
     * @param broker
     * @return
     */
    @RequestMapping(value = "/refresh", method = RequestMethod.POST)
    public Result<?> refresh(UserInfo ui, @RequestParam(name = "cid") int cid) {
        logger.info("refresh broker info cid =" + cid);
        Result<List<Broker>> brokerListResult = mqAdminTemplate.execute(new MQAdminCallback<Result<List<Broker>>>() {
            public Result<List<Broker>> callback(MQAdminExt mqAdmin) throws Exception {
                // 获取集群信息
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                // 获得broker地址map
                HashMap<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
                if (brokerAddrTable.isEmpty()) {
                    return Result.getResult(Status.NO_RESULT);
                }
                List<Broker> list = new ArrayList<Broker>();
                // 遍历集群中所有的broker
                for (String brokerName : brokerAddrTable.keySet()) {
                    HashMap<Long, String> brokerAddrs = brokerAddrTable.get(brokerName).getBrokerAddrs();
                    for (Long brokerId : brokerAddrs.keySet()) {
                        Broker broker = new Broker();
                        broker.setBrokerName(brokerName);
                        broker.setAddr(brokerAddrs.get(brokerId));
                        broker.setBrokerID(brokerId.intValue());
                        broker.setCid(cid);
                        list.add(broker);
                    }
                }
                return Result.getResult(list);
            }

            public Result<List<Broker>> exception(Exception e) throws Exception {
                logger.error("examineBroker cid:{}", cid, e);
                return Result.getWebErrorResult(e);
            }

            public Cluster mqCluster() {
                return getMQCluster(cid);
            }
        });
        if (brokerListResult.isEmpty()) {
            return brokerListResult;
        }
        Result<?> result = brokerService.refresh(cid, brokerListResult.getResult());
        return Result.getWebResult(result);
    }
    
    private Cluster getMQCluster(Integer cid) {
        Cluster mqCluster = null;
        if (cid != null) {
            mqCluster = clusterService.getMQClusterById(cid);
        }
        if (mqCluster == null && clusterService.getAllMQCluster() != null) {
            mqCluster = clusterService.getAllMQCluster()[0];
        }
        return mqCluster;
    }
}
