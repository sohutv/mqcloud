package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.admin.TopicOffset;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.Connection;
import org.apache.rocketmq.common.protocol.body.ConsumeStatus;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.common.protocol.body.GroupList;
import org.apache.rocketmq.common.protocol.body.ProcessQueueInfo;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.protocol.LanguageCode;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.ConsumeStatsExt;
import com.sohu.tv.mq.cloud.bo.Consumer;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.dao.ConsumerDao;
import com.sohu.tv.mq.cloud.dao.UserConsumerDao;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.DefaultInvoke;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
/**
 * consumer服务
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月27日
 */
@Service
public class ConsumerService {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ConsumerDao consumerDao;
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    @Autowired
    private UserConsumerDao userConsumerDao;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private TopicService topicService;
    
    /**
     * 保存Consumer记录
     * 
     * @param consumer
     * @return 返回Result
     */
    @Transactional
    public Integer save(Consumer consumer) {
        try {
            return consumerDao.insert(consumer);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", consumer.getName());
            throw e;
        } catch (Exception e) {
            logger.error("insert err, consumer:{}", consumer, e);
            throw e;
        }
    }

    /**
     * 根据cluster列表获取consumer
     * 
     * @param mqCluster
     * @return Result<List<Consumer>>
     */
    public Result<List<Consumer>> queryConsumerList(Cluster mqCluster) {
        List<Consumer> list = null;
        try {
            list = consumerDao.selectByClusterId(mqCluster.getId());
        } catch (Exception e) {
            logger.error("queryConsumerList err, mqCluster:{}", mqCluster, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(list);
    }

    /**
     * 根据id获取consumer
     * 
     * @param long
     * @return Result<Consumer>
     */
    public Result<Consumer> queryById(long id) {
        Consumer consumer = null;
        try {
            consumer = consumerDao.selectById(id);
        } catch (Exception e) {
            logger.error("queryById err:{}", id, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(consumer);
    }
    
    /**
     * 根据name获取consumer
     * 
     * @param long
     * @return Result<Consumer>
     */
    public Result<Consumer> queryConsumerByName(String name) {
        Consumer consumer = null;
        try {
            consumer = consumerDao.selectByName(name);
        } catch (Exception e) {
            logger.error("queryConsumerByName err:{}", name, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(consumer);
    }
    
    /**
     * 按照tid查询consumer
     * 
     * @param Result<List<Consumer>>
     */
    public Result<List<Consumer>> queryByTid(long tid) {
        List<Long> tidList = new ArrayList<Long>(1);
        tidList.add(tid);
        return queryByTidList(tidList);
    }
    
    /**
     * 查询全量consumer
     * 
     * @param Result<List<Consumer>>
     */
    public Result<List<Consumer>> queryAll() {
        List<Consumer> consumerList = null;
        try {
            consumerList = consumerDao.selectAll();
        } catch (Exception e) {
            logger.error("queryAll", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(consumerList);
    }
    
    /**
     * 按照tid列表查询consumer
     * 
     * @param Result<List<Consumer>>
     */
    public Result<List<Consumer>> queryByTidList(Collection<Long> tidCollection) {
        List<Consumer> consumerList = null;
        try {
            consumerList = consumerDao.selectByTidList(tidCollection);
        } catch (Exception e) {
            logger.error("queryByTidList err, idCollection:{}", tidCollection, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(consumerList);
    }
    
    /**
     * 按照id列表查询consumer
     * 
     * @param Result<List<Consumer>>
     */
    public Result<List<Consumer>> queryByIdList(Collection<Long> idCollection) {
        List<Consumer> consumerList = null;
        try {
            consumerList = consumerDao.selectByIdList(idCollection);
        } catch (Exception e) {
            logger.error("queryByIdList err, idCollection:{}", idCollection, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(consumerList);
    }
    
    /**
     * 查询用户所属topic的消费者
     * 
     * @param Result<List<Consumer>>
     */
    public Result<List<Consumer>> queryUserTopicConsumer(long uid, long tid) {
        List<Consumer> consumerList = null;
        try {
            consumerList = consumerDao.select(uid, tid);
        } catch (Exception e) {
            logger.error("queryUserTopicConsumer err, uid:{}, tid:{}", uid, tid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(consumerList);
    }
    
    /**
     * 查询用户的消费者
     * 
     * @param Result<List<Consumer>>
     */
    public Result<List<Consumer>> queryUserConsumer(long uid) {
        List<Consumer> consumerList = null;
        try {
            consumerList = consumerDao.selectByUid(uid);
        } catch (Exception e) {
            logger.error("queryUserConsumer err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(consumerList);
    }
    
    /**
     * 按照tid和consumer查询consumer
     * 
     * @param Result<List<Consumer>>
     */
    public Result<Consumer> queryTopicConsumerByName(long tid, String consumerName) {
        Consumer consumer = null;
        try {
            consumer = consumerDao.selectTopicConsumerByName(consumerName, tid);
        } catch (Exception e) {
            logger.error("queryTopicConsumerByName err, tid:{}, consumer:{}", tid, consumerName, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(consumer);
    }
    
    /**
     * 更新consumer描述
     * @return
     */
    public Result<Integer> updateConsumerInfo(long id, String info) {
        Integer result = null;
        try {
            result = consumerDao.updateConsumerInfo(id, info);
        } catch (Exception e) {
            logger.error("updateConsumerInfo err, id:{}, info:{}", id, info, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 更新consumer trace
     * @return
     */
    public Result<Integer> updateConsumerTrace(long id, int traceEnabled) {
        Integer result = null;
        try {
            result = consumerDao.updateConsumerTrace(id, traceEnabled);
        } catch (Exception e) {
            logger.error("updateConsumerTrace err, id:{}, traceEnabled:{}", id, traceEnabled, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 抓取集群消费方式的消费者进度
     * @param topic
     * @param consumerList
     * @return
     */
    public Map<Long, ConsumeStats> fetchClusteringConsumeProgress(Cluster cluster, List<Consumer> consumerList) {
        return mqAdminTemplate.execute(new DefaultCallback<Map<Long, ConsumeStats>>() {
            public Map<Long, ConsumeStats> callback(MQAdminExt mqAdmin) throws Exception {
                Map<Long, ConsumeStats> map = new HashMap<Long, ConsumeStats>();
                for(Consumer consumer : consumerList) {
                    if(consumer.isClustering()) {
                        try {
                            ConsumeStats consumeStats = mqAdmin.examineConsumeStats(consumer.getName());
                            map.put(consumer.getId(), consumeStats);
                        } catch (Exception e) {
                            logger.warn("examineConsumeStats:{} err:{}", consumer.getName(), e.getMessage());
                        }
                    }
                }
                return map;
            }
            @Override
            public Cluster mqCluster() {
                return cluster;
            }
        });
    }
    
    /**
     * 抓取广播消费方式的消费进度
     * @param topic
     * @param consumerList
     * @return
     */
    public Map<Long, List<ConsumeStatsExt>> fetchBroadcastingConsumeProgress(Cluster cluster, String topic, List<Consumer> consumerList) {
        return mqAdminTemplate.execute(new DefaultCallback<Map<Long, List<ConsumeStatsExt>>>() {
            public Map<Long, List<ConsumeStatsExt>> callback(MQAdminExt mqAdmin) throws Exception {
                Map<Long, List<ConsumeStatsExt>> map = new HashMap<Long, List<ConsumeStatsExt>>();
                TopicStatsTable topicStatsTable = mqAdmin.examineTopicStats(topic);
                if(topicStatsTable == null) {
                    return map;
                }
                for(Consumer consumer : consumerList) {
                    if(!consumer.isBroadcast()) {
                        continue;
                    }
                    ConsumerConnection cc = null;
                    try {
                        cc = mqAdmin.examineConsumerConnectionInfo(consumer.getName());
                    } catch (MQBrokerException e) {
                        logger.warn("examineConsumerConnectionInfo {} err:{}", consumer.getName(), e.getMessage());
                        continue;
                    }
                    Set<Connection> connSet = cc.getConnectionSet();
                    // only for fixed order
                    Set<String> clientIdSet = new TreeSet<String>();
                    for(Connection conn : connSet) {
                        if (conn.getVersion() < MQVersion.Version.V3_1_8_SNAPSHOT.ordinal()) {
                            continue;
                        }
                        clientIdSet.add(conn.getClientId());
                    }
                    List<ConsumeStatsExt> consumeStatsList = new ArrayList<ConsumeStatsExt>();
                    for(String clientId : clientIdSet) {
                        // 抓取状态
                        ConsumerRunningInfo consumerRunningInfo = mqAdmin.getConsumerRunningInfo(consumer.getName(), clientId, false);
                        Map<MessageQueue, ProcessQueueInfo> mqProcessMap = consumerRunningInfo.getMqTable();
                        if(mqProcessMap == null) {
                            continue;
                        }
                        // 组装数据
                        Map<MessageQueue, OffsetWrapper> offsetTable = new TreeMap<MessageQueue, OffsetWrapper>();
                        for(MessageQueue mq : mqProcessMap.keySet()) {
                            TopicOffset topicOffset = topicStatsTable.getOffsetTable().get(mq);
                            if(topicOffset == null) {
                                continue;
                            }
                            OffsetWrapper offsetWrapper = new OffsetWrapper();
                            offsetWrapper.setBrokerOffset(topicOffset.getMaxOffset());
                            ProcessQueueInfo processQueueInfo = mqProcessMap.get(mq);
                            offsetWrapper.setConsumerOffset(processQueueInfo.getCommitOffset());
                            offsetWrapper.setLastTimestamp(processQueueInfo.getLastConsumeTimestamp());
                            offsetTable.put(mq, offsetWrapper);
                        }
                        ConsumeStatsExt consumeStats = new ConsumeStatsExt();
                        consumeStats.setOffsetTable(offsetTable);
                        consumeStats.setClientId(clientId);
                        // 计算tps
                        if(consumerRunningInfo.getStatusTable() != null) {
                            ConsumeStatus consumeStatus = consumerRunningInfo.getStatusTable().get(topic);
                            if(consumeStatus != null) {
                                consumeStats.setConsumeTps(consumeStatus.getConsumeOKTPS() + consumeStatus.getConsumeFailedTPS());
                            }
                        }
                        consumeStatsList.add(consumeStats);
                    }
                    map.put(consumer.getId(), consumeStatsList);
                }
                return map;
            }
            @Override
            public Cluster mqCluster() {
                return cluster;
            }
        });
    }
    
    /**
     * 删除consumer
     */
    @Transactional
    public Result<?> deleteConsumer(Cluster mqCluster, Consumer consumer, long uid) {
        try {
            // 第一步：删除consumer记录
            Integer count = consumerDao.delete(consumer.getId());
            if(count == null || count != 1) {
                return Result.getResult(Status.DB_ERROR);
            }
            // 第二步：删除UserConsumer
            Integer deleteCount = userConsumerDao.deleteByConsumerId(consumer.getId());
            if(deleteCount == null) {
                return Result.getResult(Status.DB_ERROR);
            }
            // 第三步：真实删除consumer(为了防止误删，只有线上环境才能删除)
            if(mqCloudConfigHelper.isOnline()) {
                Result<?> result = deleteConsumerOnCluster(mqCluster, consumer.getName());
                if(result.isNotOK()) {
                    throw new RuntimeException("delete consumer:"+consumer.getName()+" on cluster err!");
                }
                if(consumer.isClustering()) {
                    topicService.deleteTopicOnCluster(mqCluster, MixAll.RETRY_GROUP_TOPIC_PREFIX + consumer.getName());
                }
            }
        } catch (Exception e) {
            logger.error("deleteConsumer:{}", consumer.getName(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getWebErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 删除consumer
     * @param mqCluster
     * @param consumerGroup
     */
    public Result<?> deleteConsumerOnCluster(Cluster mqCluster, String consumerGroup) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<?>>() {
            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                long start = System.currentTimeMillis();
                Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(mqAdmin, mqCluster.getName());
                for (String master : masterSet) {
                    mqAdmin.deleteSubscriptionGroup(master, consumerGroup);
                }
                long end = System.currentTimeMillis();
                logger.info("delete consumer use:{}ms,consumerGroup:{},cluster:{}", (end- start), consumerGroup, mqCluster);
                return Result.getOKResult();
            }
            @Override
            public Result<?> exception(Exception e) throws Exception {
                logger.error("delete consumer:{} err:{}", consumerGroup, e.getMessage());
                return Result.getWebErrorResult(e);
            }
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }
    
    /**
     * 重置offset
     * 
     * @param clusterId
     * @param topic
     * @param consumer
     * @param time
     * @return
     */
    public Result<?> resetOffset(long clusterId, String topic, String consumer, String time) {
        // 解析重置的时间
        long resetTo = -1;
        if (StringUtils.isNotBlank(time)) {
            try {
                Date date = DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).parse(time);
                resetTo = date.getTime();
            } catch (Exception e) {
                logger.error("resetOffsetTo param err:{}", time, e);
                return Result.getResult(Status.PARAM_ERROR);
            }
        } else {
            // 跳过堆积：重置到一分钟之前
            resetTo = System.currentTimeMillis() - 60000;
        }
        if (resetTo == -1) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Cluster cluster = clusterService.getMQClusterById(clusterId);
        Result<?> resetResult = resetOffset(cluster, topic, consumer, resetTo);
        return resetResult;
    }
    
    /**
     * 重置偏移量
     * @param mqCluster
     * @param consumer
     */
    public Result<?> resetOffset(Cluster mqCluster, String topic, String consumerGroup, long timeInMillis) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<?>>() {
            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                long start = System.currentTimeMillis();
                //判断消费者是否在线
                boolean consumerOnline = true;
                try {
                    mqAdmin.examineConsumerConnectionInfo(consumerGroup);
                } catch (MQBrokerException e) {
                    if(206 == e.getResponseCode()) {
                        consumerOnline = false;
                    }
                }
                String env = null;
                if(consumerOnline) {
                    env = "online";
                    //重置consumer端, consumer在线
                    mqAdmin.resetOffsetByTimestamp(topic, consumerGroup, timeInMillis, true);
                } else {
                    //重置broker端, consumer不在线
                    env = "offline";
                    mqAdmin.resetOffsetByTimestampOld(consumerGroup, topic, timeInMillis, true);
                }
                String time = DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).format(new Date(timeInMillis));
                logger.info("resetOffset {} to {} use:{},topic={},group={}", env, time, System.currentTimeMillis()- start, 
                        topic, consumerGroup);
                return Result.getOKResult();
            }
            @Override
            public Result<?> exception(Exception e) throws Exception {
                logger.error("resetOffset topic={},group={} err:{}", topic, consumerGroup, e.getMessage());
                return Result.getWebErrorResult(e);
            }
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }
    
    /**
     * 获取消费者链接
     * @param consumerGroup
     * @param mqCluster
     * @return
     */
    public Result<ConsumerConnection> examineConsumerConnectionInfo(String consumerGroup, Cluster mqCluster) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<ConsumerConnection>>() {
            public Result<ConsumerConnection> callback(MQAdminExt mqAdmin) throws Exception {
                ConsumerConnection consumerConnection = mqAdmin.examineConsumerConnectionInfo(consumerGroup);
                return Result.getResult(consumerConnection);
            }
            public Result<ConsumerConnection> exception(Exception e) throws Exception {
                logger.warn("cluster:{} consumerGroup:{} error:{}", mqCluster, consumerGroup, e.getMessage());
                return Result.getDBErrorResult(e);
            }
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }
    
    /**
     * 初始化consumer（从集群导入到数据库中），可以执行多次，因为数据库有唯一索引
     * 该方法适用于公司内部已经搭建了mq集群，想使用mqcloud进行管理
     * @param mqCluster
     * @param topicList
     * @return
     */
    @SuppressWarnings("rawtypes")
    public Map<String, List<Result>> initConsumer(Cluster mqCluster, List<Topic> topicList) {
        Map<String, List<Result>> resultMap = new HashMap<String, List<Result>>();
        mqAdminTemplate.execute(new DefaultInvoke() {
            public Cluster mqCluster() {
                return mqCluster;
            }
            public void invoke(MQAdminExt mqAdmin) throws Exception {
                for (Topic topic : topicList) {
                    GroupList groupList = null;
                    try {
                        groupList = mqAdmin.queryTopicConsumeByWho(topic.getName());
                    } catch (Exception e) {
                        logger.error("queryTopicConsumeByWho, topic:{}", topic.getName(), e);
                        addToMap(resultMap, topic.getName(), Result.getWebErrorResult(e).setResult(topic.getName()));
                        continue;
                    }
                    for (String group : groupList.getGroupList()) {
                        ConsumerConnection conn = null;
                        try {
                            conn = mqAdmin.examineConsumerConnectionInfo(group);
                        } catch (MQBrokerException e) {
                            if (206 == e.getResponseCode()) {
                                addToMap(resultMap, topic.getName(), Result.getResult(Status.NO_ONLINE).setResult(group));
                                continue;
                            } else {
                                addToMap(resultMap, topic.getName(), Result.getWebErrorResult(e).setResult(group));
                            }
                        } catch (Exception e) {
                            addToMap(resultMap, topic.getName(), Result.getWebErrorResult(e).setResult(group));
                            logger.error("topic:{} consuemr:{} connect err", topic, group, e);
                        }
                        if (conn == null) {
                            addToMap(resultMap, topic.getName(), Result.getResult(Status.NO_ONLINE).setResult(group));
                            continue;
                        }
                        Consumer consumer = new Consumer();
                        if (MessageModel.BROADCASTING == conn.getMessageModel()) {
                            consumer.setConsumeWay(Consumer.BROADCAST);
                        }
                        consumer.setName(group);
                        consumer.setTid(topic.getId());
                        try {
                            save(consumer);
                            addToMap(resultMap, topic.getName(), Result.getResult(group));
                        } catch (Exception e) {
                            logger.error("topic:{} consuemr:{} save err", topic, group, e);
                            addToMap(resultMap, topic.getName(), Result.getWebErrorResult(e).setResult(group));
                        }
                    }
                }
            }
        });
        return resultMap;
    }
    
    @SuppressWarnings("rawtypes")
    private void addToMap(Map<String, List<Result>> resultMap, String topic, Result<?> result) {
        List<Result> resultList = resultMap.get(topic);
        if(resultList == null) {
            resultList = new ArrayList<Result>();
            resultMap.put(topic, resultList);
        }
        resultList.add(result);
    }
    
    /**
     * 获取consumer运行时信息
     * @param cluster
     * @param consumerGroup
     * @return
     */
    public Map<String, ConsumerRunningInfo> getConsumerRunningInfo(Cluster cluster, String consumerGroup) {
        return mqAdminTemplate.execute(new MQAdminCallback<Map<String, ConsumerRunningInfo>>() {
            public Map<String, ConsumerRunningInfo> callback(MQAdminExt mqAdmin) throws Exception {
                Map<String, ConsumerRunningInfo> infoMap = new HashMap<String, ConsumerRunningInfo>();
                // 获取consumer链接
                ConsumerConnection consumerConnection = mqAdmin.examineConsumerConnectionInfo(consumerGroup);
                // 获取运行时信息
                for (Connection connection : consumerConnection.getConnectionSet()) {
                    if (connection.getVersion() < MQVersion.Version.V3_1_8_SNAPSHOT.ordinal()) {
                        continue;
                    }
                    String clientId = connection.getClientId();
                    ConsumerRunningInfo consumerRunningInfo = mqAdmin.getConsumerRunningInfo(consumerGroup, clientId, false);
                    if(consumerRunningInfo != null) {
                        infoMap.put(clientId, consumerRunningInfo);
                    }
                }
                return infoMap;
            }
            public Map<String, ConsumerRunningInfo> exception(Exception e) throws Exception {
                logger.warn("cluster:{}, consumer:{}, err:{}", cluster, consumerGroup, e.getMessage());
                return null;
            }
            @Override
            public Cluster mqCluster() {
                return cluster;
            }
        });
    }
    
    /**
     * 获取消费者状态（go客户端不支持getConsumeStatus，所以调用getConsumerRunningInfo）
     * 
     * @param mqAdmin
     * @param topic
     * @param consumer
     * @param connection
     * @return
     */
    public Map<MessageQueue, Long> fetchConsumerStatus(MQAdminExt mqAdmin, String topic, String consumer,
            Connection connection) {
        if (LanguageCode.GO == connection.getLanguage()) {
            ConsumerRunningInfo info;
            try {
                info = mqAdmin.getConsumerRunningInfo(consumer, connection.getClientId(), false);
            } catch (Exception e) {
                logger.error("getConsumerRunningInfo topic:{},consumerGroup:{}", topic, consumer, e);
                return null;
            }
            if (info == null) {
                return null;
            }
            if (info.getMqTable() == null) {
                return null;
            }
            Map<MessageQueue, Long> consumerOffsetMap = new HashMap<>();
            for (MessageQueue messageQueue : info.getMqTable().keySet()) {
                consumerOffsetMap.put(messageQueue, info.getMqTable().get(messageQueue).getCommitOffset());
            }
            return consumerOffsetMap;
        }
        try {
            Map<String, Map<MessageQueue, Long>> consumerStatusTable = mqAdmin.getConsumeStatus(topic, consumer,
                    connection.getClientId());
            return consumerStatusTable.get(connection.getClientId());
        } catch (Exception e) {
            logger.error("getConsumeStatus topic:{},consumerGroup:{}", topic, consumer, e);
            return null;
        }
    }
}

