package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.common.model.*;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.dao.BrokerDao;
import com.sohu.tv.mq.cloud.dao.BrokerTmpDao;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.DefaultSohuMQAdmin;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.DBUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.BrokerConfigUpdateParam;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.protocol.RemotingSysResponseCode;
import org.apache.rocketmq.remoting.protocol.body.BrokerStatsData;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.apache.rocketmq.remoting.protocol.body.ProducerTableInfo;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * broker
 * 
 * @author yongfeigao
 * @date 2018年11月14日
 */
@Service
public class BrokerService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BrokerDao brokerDao;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private SqlSessionFactory mqSqlSessionFactory;

    @Autowired
    private BrokerTmpDao brokerTmpDao;

    /**
     * 查询集群的broker
     * 
     * @return Result<List<Broker>>
     */
    public Result<List<Broker>> query(int cid) {
        List<Broker> result = null;
        try {
            result = brokerDao.selectByClusterId(cid);
            if (result != null && result.size() == 0) {
                result = null;
            }
        } catch (Exception e) {
            logger.error("query cid:{} err", cid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 查询所有的broker
     *
     * @return Result<List<Broker>>
     */
    public Result<List<Broker>> queryAll() {
        try {
            return Result.getResult(brokerDao.selectAll());
        } catch (Exception e) {
            logger.error("query all err", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 删除记录
     * 
     * @return 返回Result
     */
    public Result<?> delete(int cid) {
        Integer result = null;
        try {
            result = brokerDao.delete(cid);
        } catch (Exception e) {
            logger.error("delete err, cid:{}", cid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 刷新记录
     * 
     * @return 返回Result
     */
    public Result<?> refresh(int cid, List<Broker> brokerList) {
        try {
            brokerDao.delete(cid);
            for (Broker broker : brokerList) {
                brokerDao.insert(broker);
            }
        } catch (Exception e) {
            logger.error("refresh err, cid:{}", cid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 更新记录
     * 
     * @param cid
     * @param addr
     * @return
     */
    public Result<?> update(int cid, String addr, CheckStatusEnum checkStatusEnum) {
        Integer result = null;
        try {
            result = brokerDao.update(cid, addr, checkStatusEnum.getStatus());
        } catch (Exception e) {
            logger.error("update err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 抓取broker配置
     * @param cid
     * @param brokerAddr
     * @return
     */
    public Result<Properties> fetchBrokerConfig(int cid, String brokerAddr) {
        Cluster cluster = clusterService.getMQClusterById(cid);
        return fetchBrokerConfig(cluster, brokerAddr);
    }

    /**
     * 抓取broker配置
     * @param cluster
     * @param brokerAddr
     * @return
     */
    public Result<Properties> fetchBrokerConfig(Cluster cluster, String brokerAddr) {
        return mqAdminTemplate.execute(new DefaultCallback<Result<Properties>>() {
            public Cluster mqCluster() {
                return cluster;
            }

            public Result<Properties> callback(MQAdminExt mqAdmin) throws Exception {
                Properties properties = mqAdmin.getBrokerConfig(brokerAddr);
                return Result.getResult(properties);
            }

            public Result<Properties> exception(Exception e) {
                logger.error("cluster:{} brokerAddr:{}, getBrokerConfig err", cluster, brokerAddr, e);
                return Result.getDBErrorResult(e);
            }
        });
    }
    
    /**
     * 更新broker配置
     * @param cid
     * @param brokerAddr
     * @return
     */
    public Result<?> updateBrokerConfig(BrokerConfigUpdateParam brokerConfigUpdateParam) {
        return mqAdminTemplate.execute(new DefaultCallback<Result<?>>() {
            public Cluster mqCluster() {
                return clusterService.getMQClusterById(brokerConfigUpdateParam.getCid());
            }

            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                Properties properties = brokerConfigUpdateParam.getConfigProperties();
                if (brokerConfigUpdateParam.isCluster()) {
                    Result<List<Broker>> result = query(brokerConfigUpdateParam.getCid());
                    if (result.isNotOK()) {
                        return result;
                    }
                    List<Broker> brokers = result.getResult();
                    for (Broker broker : brokers) {
                        mqAdmin.updateBrokerConfig(broker.getAddr(), properties);
                    }
                } else {
                    mqAdmin.updateBrokerConfig(brokerConfigUpdateParam.getAddr(), properties);
                }
                return Result.getOKResult();
            }

            public Result<?> exception(Exception e) {
                logger.error("brokerConfigUpdateParam:{} updateBrokerConfig err", brokerConfigUpdateParam, e);
                return Result.getDBErrorResult(e);
            }
        });
    }

    /**
     * 获取发送消息限速情况
     * @param cid
     * @param brokerAddr
     * @return
     */
    public Result<BrokerRateLimitData> fetchSendMessageRateLimitInBroker(int cid, String brokerAddr) {
        return mqAdminTemplate.execute(new DefaultCallback<Result<BrokerRateLimitData>>() {
            public Cluster mqCluster() {
                return clusterService.getMQClusterById(cid);
            }

            public Result<BrokerRateLimitData> callback(MQAdminExt mqAdmin) throws Exception {
                BrokerRateLimitData brokerRateLimitData =
                        ((SohuMQAdmin) mqAdmin).fetchSendMessageRateLimitInBroker(brokerAddr);
                if (brokerRateLimitData != null) {
                    List<TopicRateLimit> list = brokerRateLimitData.getTopicRateLimitList();
                    if (list != null) {
                        Collections.sort(list, (t1, t2) -> {
                            // 等待时长逆序
                            if (t1.getLastNeedWaitMicrosecs() > t2.getLastNeedWaitMicrosecs()) {
                                return -1;
                            }
                            if (t1.getLastNeedWaitMicrosecs() < t2.getLastNeedWaitMicrosecs()) {
                                return 1;
                            }
                            // 限流时间逆序
                            if (t1.getLastRateLimitTimestamp() > t2.getLastRateLimitTimestamp()) {
                                return -1;
                            }
                            if (t1.getLastRateLimitTimestamp() < t2.getLastRateLimitTimestamp()) {
                                return 1;
                            }
                            return 0;
                        });
                    }
                }
                return Result.getResult(brokerRateLimitData);
            }

            public Result<BrokerRateLimitData> exception(Exception e) {
                logger.warn("cid:{}, brokerAddr:{}, fetchSendMessageRateLimitInBroker err:{}", cid, brokerAddr, e.toString());
                // 判断是否支持
                if (e instanceof MQClientException && ((MQClientException) e).getResponseCode() == RemotingSysResponseCode.REQUEST_CODE_NOT_SUPPORTED) {
                    Result<BrokerRateLimitData> result = Result.getResult(Status.BROKER_UNSUPPORTED_ERROR);
                    return result.setException(e);
                }
                return Result.getDBErrorResult(e);
            }
        });
    }

    /**
     * 更新限速
     * @param cid
     * @param brokerAddr -1表示更新整个集群
     * @param updateSendMsgRateLimitRequestHeader
     */
    public Result<?> updateSendMessageRateLimit(int cid, String brokerAddr, UpdateSendMsgRateLimitRequestHeader updateSendMsgRateLimitRequestHeader) {
        return mqAdminTemplate.execute(new DefaultCallback<Result<Void>>() {
            public Cluster mqCluster() {
                return clusterService.getMQClusterById(cid);
            }

            public Result<Void> callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                if ("-1".equals(brokerAddr)) {
                    Result<List<Broker>> result = query(cid);
                    List<Broker> brokers = result.getResult();
                    for (Broker broker : brokers) {
                        sohuMQAdmin.updateSendMessageRateLimit(broker.getAddr(), updateSendMsgRateLimitRequestHeader);
                    }
                } else {
                    sohuMQAdmin.updateSendMessageRateLimit(brokerAddr, updateSendMsgRateLimitRequestHeader);
                }
                return Result.getOKResult();
            }

            public Result<Void> exception(Exception e) {
                logger.error("cid:{}, brokerAddr:{}, param:{} updateSendMessageRateLimit err", cid, brokerAddr, updateSendMsgRateLimitRequestHeader, e);
                return Result.getDBErrorResult(e);
            }
        });
    }

    /**
     * 查询broker
     */
    public Result<Broker> queryBroker(int cid, String addr) {
        try {
            return Result.getResult(brokerDao.selectBroker(cid, addr));
        } catch (Exception e) {
            logger.error("queryBroker:{} err", addr, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询临时broker
     */
    public Result<List<Broker>> queryTmpBroker(int cid) {
        try {
            return Result.getResult(brokerTmpDao.selectByClusterId(cid));
        } catch (Exception e) {
            logger.error("queryTmpBroker:{} err", cid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询临时broker
     */
    public Result<Broker> queryTmpBroker(int cid, String addr) {
        try {
            return Result.getResult(brokerTmpDao.select(cid, addr));
        } catch (Exception e) {
            logger.error("queryTmpBroker cid:{} addr:{} err", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 更新是否可写
     */
    public Result<?> updateWritable(int cid, String addr, boolean writable) {
        Integer result = null;
        try {
            if (writable) {
                result = brokerDao.updateWritable(cid, addr, 1);
            } else {
                result = brokerDao.updateWritable(cid, addr, 0);
            }
        } catch (Exception e) {
            logger.error("updateWritable err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }

    /**
     * 获取broker的timerWheel信息
     */
    public Result<?> getTimerWheelMetrics(int cid, String brokerAddr) {
        return mqAdminTemplate.execute(new DefaultCallback<Result<Void>>() {
            public Cluster mqCluster() {
                return clusterService.getMQClusterById(cid);
            }

            public Result<Void> callback(MQAdminExt mqAdmin) throws Exception {
                TimerMetricsSerializeWrapper timerMetricsSerializeWrapper = ((SohuMQAdmin) mqAdmin).getTimerWheelMetrics(brokerAddr);
                return Result.getResult(timerMetricsSerializeWrapper);
            }

            public Result<Void> exception(Exception e) {
                logger.error("cid:{}, brokerAddr:{} getTimerWheelMetrics err", cid, brokerAddr, e);
                if (e instanceof MQBrokerException &&
                        ((MQBrokerException) e).getResponseCode() == RemotingSysResponseCode.REQUEST_CODE_NOT_SUPPORTED) {
                    return Result.getResult(Status.BROKER_UNSUPPORTED_ERROR);
                }
                return Result.getDBErrorResult(e);
            }
        });
    }

    /**
     * 重置topic流量
     */
    public Result<Integer> resetDayCount() {
        try {
            return Result.getResult(brokerDao.resetDayCount());
        } catch (Exception e) {
            logger.error("resetDayCount err", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 更新topic日流量
     *
     * @param brokerTrafficList
     * @return
     */
    public Result<Integer> updateDayCount(List<BrokerTraffic> brokerTrafficList) {
        return DBUtil.batchUpdate(mqSqlSessionFactory, BrokerDao.class, dao -> {
            for (BrokerTraffic brokerTraffic : brokerTrafficList) {
                dao.updateDayCount(brokerTraffic);
            }
        });
    }

    /**
     * 保存broker到临时表
     */
    public Result<?> saveBrokerTmp(Map<String, Object> param){
        Object brokerName = param.get("brokerName");
        if (brokerName == null) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Object brokerIdObj = param.get("brokerId");
        if (brokerIdObj == null) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        int brokerId = NumberUtils.toInt(String.valueOf(brokerIdObj), -1);
        if (brokerId == -1) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Object ipObj = param.get("ip");
        if (ipObj == null) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Object portObj = param.get("listenPort");
        if (portObj == null) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Object dirObj = param.get("dir");
        if (dirObj == null) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Object cidObj = param.get("rmqAddressServerSubGroup");
        if (cidObj == null) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        int cid = NumberUtils.toInt(String.valueOf(cidObj).split("-")[1]);
        if (cid == 0) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        // 保存broker到临时表
        Broker broker = new Broker();
        broker.setBrokerName(brokerName.toString());
        broker.setAddr(String.valueOf(ipObj) + ":" + String.valueOf(portObj));
        broker.setBrokerID(brokerId);
        broker.setCid(cid);
        broker.setBaseDir(String.valueOf(dirObj));
        try {
            return Result.getResult(brokerTmpDao.insert(broker));
        } catch (Exception e) {
            logger.error("saveBrokerTmp:{}", param, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 删除broker到临时表
     */
    public Result<?> deleteBrokerTmp(int cid, String addr) {
        try {
            return Result.getResult(brokerTmpDao.delete(cid, addr));
        } catch (Exception e) {
            logger.error("deleteBrokerTmp:{}", addr, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询broker的运行时统计
     */
    public Result<KVTable> fetchBrokerRuntimeStats(String brokerAddr, Cluster mqCluster) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<KVTable>>() {
            public Result<KVTable> callback(MQAdminExt mqAdmin) throws Exception {
                return Result.getResult(mqAdmin.fetchBrokerRuntimeStats(brokerAddr));
            }

            public Cluster mqCluster() {
                return mqCluster;
            }

            public Result<KVTable> exception(Exception e) throws Exception {
                logger.error("fetchBrokerRuntimeStats:{} err", brokerAddr, e);
                return Result.getDBErrorResult(e);
            }
        });
    }

    /**
     * 查询producer连接
     */
    public Result<ClientConnectionInfo> fetchAllProducerConnection(String brokerAddr, Cluster mqCluster) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<ClientConnectionInfo>>() {
            public Result<ClientConnectionInfo> callback(MQAdminExt mqAdmin) throws Exception {
                DefaultSohuMQAdmin sohuMQAdmin = (DefaultSohuMQAdmin) mqAdmin;
                ProducerTableInfo producerTableInfo = sohuMQAdmin.getAllProducerInfo(brokerAddr, true);
                return Result.getResult(ClientConnectionInfo.build(producerTableInfo));
            }

            public Cluster mqCluster() {
                return mqCluster;
            }

            public Result<ClientConnectionInfo> exception(Exception e) throws Exception {
                logger.error("fetchAllProducerConnection:{} err", brokerAddr, e);
                return Result.getDBErrorResult(e);
            }
        });
    }

    /**
     * 查询consumer连接
     */
    public Result<ClientConnectionInfo> fetchAllConsumerConnection(String brokerAddr, Cluster mqCluster) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<ClientConnectionInfo>>() {
            public Result<ClientConnectionInfo> callback(MQAdminExt mqAdmin) throws Exception {
                DefaultSohuMQAdmin sohuMQAdmin = (DefaultSohuMQAdmin) mqAdmin;
                ConsumerTableInfo consumerTableInfo = sohuMQAdmin.getAllConsumerInfo(brokerAddr, true);
                return Result.getResult(ClientConnectionInfo.build(consumerTableInfo));
            }

            public Cluster mqCluster() {
                return mqCluster;
            }

            public Result<ClientConnectionInfo> exception(Exception e) throws Exception {
                logger.error("fetchAllConsumerConnection:{} err", brokerAddr, e);
                return Result.getDBErrorResult(e);
            }
        });
    }

    /**
     * 查看broker统计数据，只包括外部流量
     */
    public Result<BrokerStatsData> viewBrokerPutStats(int cid, String brokerAddr) {
        Cluster cluster = clusterService.getMQClusterById(cid);
        return viewBrokerStatsData(cluster, brokerAddr, "BROKER_PUT_NUMS_FROM_EXTERNAL", cluster.getName());
    }

    /**
     * 查看broker统计数据
     */
    public Result<BrokerStatsData> viewBrokerStatsData(Cluster cluster, String brokerAddr, String statsName, String statsKey) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<BrokerStatsData>>() {
            public Result<BrokerStatsData> callback(MQAdminExt mqAdmin) throws Exception {
                return Result.getResult(mqAdmin.viewBrokerStatsData(brokerAddr, statsName, statsKey));
            }

            @Override
            public Result<BrokerStatsData> exception(Exception e) throws Exception {
                logger.error("viewBrokerStatsData broker:{} err", brokerAddr, e);
                return Result.getDBErrorResult(e);
            }

            public Cluster mqCluster() {
                return cluster;
            }
        });
    }

    /**
     * 擦除写权限
     */
    public Result<?> wipeWritePerm(int cid, String brokerName, String brokerAddr) {
        Result<Integer> rst = mqAdminTemplate.execute(new MQAdminCallback<Result<Integer>>() {
            public Result<Integer> callback(MQAdminExt mqAdmin) throws Exception {
                List<String> namesrvList = mqAdmin.getNameServerAddressList();
                if (namesrvList == null) {
                    return Result.getResult(Status.NO_RESULT).setMessage("namesrvList is empty");
                }
                int totalWipeTopicCount = 0;
                for (String namesrvAddr : namesrvList) {
                    int wipeTopicCount = mqAdmin.wipeWritePermOfBroker(namesrvAddr, brokerName);
                    totalWipeTopicCount += wipeTopicCount;
                }
                return Result.getResult(totalWipeTopicCount);
            }

            @Override
            public Result<Integer> exception(Exception e) throws Exception {
                logger.error("wipeWritePerm broker:{} err", brokerName, e);
                return Result.getDBErrorResult(e);
            }

            public Cluster mqCluster() {
                return clusterService.getMQClusterById(cid);
            }
        });
        if (rst.isOK()) {
            updateWritable(cid, brokerAddr, false);
        }
        return rst;
    }

    /**
     * 添加写权限
     */
    public Result<?> addWritePerm(Broker broker) {
        Result<Integer> rst = mqAdminTemplate.execute(new MQAdminCallback<Result<Integer>>() {
            public Result<Integer> callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                List<String> namesrvList = mqAdmin.getNameServerAddressList();
                if (namesrvList == null) {
                    return Result.getResult(Status.NO_RESULT).setMessage("namesrvList is empty");
                }
                int count = 0;
                for (String namesrvAddr : namesrvList) {
                    if (broker.isRocketMQV5()) {
                        count += sohuMQAdmin.addWritePermOfBroker(namesrvAddr, broker.getBrokerName());
                    } else {
                        sohuMQAdmin.unregisterBroker(namesrvAddr, mqCluster().getName(), broker.getAddr(), broker.getBrokerName(), broker.getBrokerID());
                    }
                }
                return Result.getResult(count);
            }

            @Override
            public Result<Integer> exception(Exception e) throws Exception {
                logger.error("addWritePerm {} err", broker, e);
                return Result.getDBErrorResult(e);
            }

            public Cluster mqCluster() {
                return clusterService.getMQClusterById(broker.getCid());
            }
        });
        if (rst.isOK()) {
            updateWritable(broker.getCid(), broker.getAddr(), true);
        }
        return rst;
    }

    /**
     * 获取broker的连接数
     */
    public Result<ClientConnectionSize> getClientConnectionSize(int cid, String brokerAddr) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<ClientConnectionSize>>() {
            public Result<ClientConnectionSize> callback(MQAdminExt mqAdmin) throws Exception {
                DefaultSohuMQAdmin sohuMQAdmin = (DefaultSohuMQAdmin) mqAdmin;
                return Result.getResult(sohuMQAdmin.getClientConnectionSize(brokerAddr));
            }

            @Override
            public Result<ClientConnectionSize> exception(Exception e) throws Exception {
                logger.error("getClientConnectionSize {} err", brokerAddr, e);
                return Result.getDBErrorResult(e);
            }

            public Cluster mqCluster() {
                return clusterService.getMQClusterById(cid);
            }
        });
    }
}
