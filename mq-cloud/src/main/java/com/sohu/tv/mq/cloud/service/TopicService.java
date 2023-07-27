package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.dao.TopicDao;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.util.CommonUtil;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;

/**
 * topic服务
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月12日
 */
@Service
public class TopicService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private TopicDao topicDao;

    @Autowired
    private UserProducerService userProducerService;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @Autowired
    private ClusterService clusterService;

    @Autowired
    private UserFootprintService userFootprintService;

    @Autowired
    private UserFavoriteService userFavoriteService;

    /**
     * 获取topic路由数据
     * 
     * @param mqCluster
     * @param topic
     * @return TopicRouteData
     */
    public TopicRouteData route(Topic topic) {
        return route(clusterService.getMQClusterById(topic.getClusterId()), topic.getName());
    }
    
    /**
     * 获取topic路由数据
     * 
     * @param mqCluster
     * @param topic
     * @return TopicRouteData
     */
    public TopicRouteData route(Cluster cluster, String topic) {
        return mqAdminTemplate.execute(new DefaultCallback<TopicRouteData>() {
            public TopicRouteData callback(MQAdminExt mqAdmin) throws Exception {
                return route(mqAdmin, topic);
            }

            public Cluster mqCluster() {
                return cluster;
            }
        });
    }

    /**
     * 获取topic路由数据
     * 
     * @param mqAdmin
     * @param topic
     * @return TopicRouteData
     * @throws InterruptedException
     * @throws MQClientException
     * @throws RemotingException
     */
    public TopicRouteData route(MQAdminExt mqAdmin, String topic)
            throws RemotingException, MQClientException, InterruptedException {
        return mqAdmin.examineTopicRouteInfo(topic);
    }

    /**
     * 根据集群获取topic列表
     * 
     * @param mqCluster
     * @return TopicRouteData
     */
    public Result<List<Topic>> queryTopicList(Cluster mqCluster) {
        List<Topic> topicList = null;
        try {
            topicList = topicDao.selectByClusterId(mqCluster.getId());
        } catch (Exception e) {
            logger.error("getTopicList err, mqCluster:{}", mqCluster, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicList);
    }

    /**
     * 根据集群获取开启了流量预警功能的topic列表
     * @param mqCluster
     * @return Result<List<Topic>>
     */
    public Result<List<Topic>> queryTrafficWarnEnabledTopicList(Cluster mqCluster) {
        List<Topic> topicList = null;
        try {
            topicList = topicDao.selectTrafficWarnEnabledTopic(mqCluster.getId());
        } catch (Exception e) {
            logger.error("queryTrafficWarnEnabledTopicList err, mqCluster:{}", mqCluster, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicList);
    }

    /**
     * 按照uid查询topic
     * 
     * @param Result<List<Topic>>
     */
    public Result<List<Topic>> queryTopicList(String topic, long uid, int offset, int size, List<Integer> traceClusterIds) {
        List<Topic> topicList = null;
        try {
            topicList = topicDao.selectByUid(topic, uid, offset, size, traceClusterIds);
        } catch (Exception e) {
            logger.error("selectByUid err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicList);
    }
    
    /**
     * 按照uid查询topic数量
     * 
     * @param Result<Integer>
     */
    public Result<Integer> queryTopicListCount(String topic, long uid, List<Integer> traceClusterIds) {
        Integer count = null;
        try {
            count = topicDao.selectByUidCount(topic, uid, traceClusterIds);
        } catch (Exception e) {
            logger.error("selectByUidCount err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(count);
    }
    
    /**
     * 按照用户查询topic状况
     * 
     * @param Result<TopicStat>
     */
    public Result<TopicStat> queryTopicStat(User user, List<Integer> traceClusterIds) {
        if(user.isAdmin()) {
            return queryTopicStat(0, traceClusterIds);
        } 
        return queryTopicStat(user.getId(), traceClusterIds);
    }
    
    /**
     * 按照uid查询topic状况
     * 
     * @param Result<TopicStat>
     */
    public Result<TopicStat> queryTopicStat(long uid, List<Integer> traceClusterIds) {
        TopicStat topicStat = null;
        try {
            topicStat = topicDao.selectTopicStat(uid, traceClusterIds);
        } catch (Exception e) {
            logger.error("queryTopicStat err, uid:{}", uid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicStat);
    }
    
    /**
     * 按照用户查询topic数量
     * 
     * @param Result<Integer>
     */
    public Result<Integer> queryTopicCount(String topic, User user, List<Integer> traceClusterIds) {
        if(user.isAdmin()) {
            return queryTopicListCount(topic, 0, traceClusterIds);
        } 
        return queryTopicListCount(topic, user.getId(), traceClusterIds);
    }
    
    /**
     * 按照用户查询topic列表
     * 
     * @param Result<Integer>
     */
    public Result<List<Topic>> queryTopicList(String topic, User user, int page, int size, List<Integer> traceClusterIds) {
        if(user.isAdmin()) {
            return queryTopicList(topic, 0, page, size, traceClusterIds);
        } 
        return queryTopicList(topic, user.getId(), page, size, traceClusterIds);
    }

    /**
     * 按照id查询topic
     * 
     * @param Result<List<Topic>>
     */
    public Result<List<Topic>> queryTopicList(Collection<Long> idCollection) {
        List<Topic> topicList = null;
        try {
            topicList = topicDao.selectByIdList(idCollection);
        } catch (Exception e) {
            logger.error("getTopicList err, idCollection:{}", idCollection, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicList);
    }
    
    /**
     * 按照name查询topic
     * 
     * @param Result<Topic>
     */
    public Result<Topic> queryTopic(String name) {
        Topic topic = null;
        try {
            topic = topicDao.selectByName(name);
        } catch (Exception e) {
            logger.error("queryTopic err, name:{}", name, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topic);
    }
    
    /**
     * 按照id查询topic
     * 
     * @param Result<List<Topic>>
     */
    public Result<Topic> queryTopic(long id) {
        List<Long> idList = new ArrayList<Long>(1);
        idList.add(id);
        Result<List<Topic>> topicListResult = queryTopicList(idList);
        if(topicListResult.isNotEmpty()) {
            return Result.getResult(topicListResult.getResult().get(0));
        }
        return Result.getResult(Status.NO_RESULT);
    }

    /**
     * 保存topic记录
     * 
     * @param topic
     * @return 返回Integer
     */
    @Transactional
    public Integer save(Topic topic) {
        try {
            return topicDao.insert(topic);
        } catch (DuplicateKeyException e) {
            logger.warn("duplicate key:{}", topic.getName());
            throw e;
        } catch (Exception e) {
            logger.error("insert err, topic:{}", topic, e);
            throw e;
        }
    }
    
    /**
     * 获取topic各个队列状态数据
     * 
     * @param mqCluster
     * @param topic
     */
    public TopicStatsTable stats(Topic topic) {
        return stats(topic.getClusterId(), topic.getName());
    }
    
    /**
     * 获取topic各个队列状态数据
     * 
     * @param mqCluster
     * @param topic
     */
    public TopicStatsTable stats(long clusterId, String topic) {
        return stats(clusterService.getMQClusterById(clusterId), topic);
    }
    
    /**
     * 获取topic各个队列状态数据
     * 
     * @param mqCluster
     * @param topic
     */
    public TopicStatsTable stats(Cluster cluster, String topic) {
        return mqAdminTemplate.execute(new MQAdminCallback<TopicStatsTable>() {
            public TopicStatsTable callback(MQAdminExt mqAdmin) throws Exception {
                TopicRouteData topicRouteData = mqAdmin.examineTopicRouteInfo(topic);
                TopicStatsTable topicStatsTable = new TopicStatsTable();
                for (BrokerData bd : topicRouteData.getBrokerDatas()) {
                    String addr = bd.selectBrokerAddr();
                    if (addr != null) {
                        TopicStatsTable tst = mqAdmin.examineTopicStats(addr, topic);
                        topicStatsTable.getOffsetTable().putAll(tst.getOffsetTable());
                    }
                }
                return topicStatsTable;
            }

            public Cluster mqCluster() {
                return cluster;
            }

            @Override
            public TopicStatsTable exception(Exception e) throws Exception {
                logger.warn("cluster:{}, topic:{}, err:{}", cluster, topic, e.getMessage());
                return null;
            }
        });
    }
    
    /**
     * 查询所有topic
     * 
     * @return TopicRouteData
     */
    public Result<List<Topic>> queryAllTopic() {
        List<Topic> topicList = null;
        try {
            topicList = topicDao.selectAll();
        } catch (Exception e) {
            logger.error("queryAllTopic err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicList);
    }
    
    /**
     * 查询非追踪的topic
     */
    public Result<List<Topic>> queryNoneTraceableTopic() {
        List<Topic> topicList = null;
        try {
            topicList = topicDao.selectNoneTraceableTopic();
        } catch (Exception e) {
            logger.error("queryNoneTraceableTopic err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicList);
    }
    
    /**
     * 创建topic
     * @param mqCluster
     * @param auditTopic
     */
    @Transactional
    public Result<?> createTopic(Cluster mqCluster, Audit audit, AuditTopic auditTopic) {
        try {
            // 第一步：创建topic记录
            Topic topic = new Topic();
            BeanUtils.copyProperties(auditTopic, topic);
            topic.setClusterId(mqCluster.getId());
            topic.setInfo(audit.getInfo());
            Integer count = save(topic);
            if(count == null) {
                return Result.getResult(Status.DB_ERROR);
            }
            // 第二步：关联producer
            UserProducer up = new UserProducer();
            up.setTid(topic.getId());
            up.setUid(audit.getUid());
            up.setProducer(auditTopic.getProducer());
            up.setProtocol(auditTopic.getProtocol());
            Integer updateCount = userProducerService.save(up);
            if(updateCount == null) {
                return Result.getResult(Status.DB_ERROR);
            }
            // 第三步：真实创建topic
            Result<?> result = createAndUpdateTopicOnCluster(mqCluster, auditTopic);
            if(result.isNotOK()) {
                throw new RuntimeException("create topic:"+auditTopic.getName()+" on cluster err!");
            }
        } catch (Exception e) {
            logger.error("createTopic cluster:{}, audit:{}, auditTopic:{}", mqCluster, audit, auditTopic, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getWebErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 创建topic
     * @param mqCluster
     * @param auditTopic
     */
    public Result<?> createAndUpdateTopicOnCluster(Cluster mqCluster, AuditTopic auditTopic) {
        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setReadQueueNums(auditTopic.getQueueNum());
        topicConfig.setWriteQueueNums(auditTopic.getQueueNum());
        topicConfig.setTopicName(auditTopic.getName());
        if(auditTopic.getOrdered() == AuditTopic.HAS_ORDER) {
            topicConfig.setOrder(true);
        }
        return createAndUpdateTopicOnCluster(mqCluster, topicConfig);
    }
    
    /**
     * 创建topic
     * @param mqCluster
     * @param auditTopic
     */
    public Result<?> createAndUpdateTopicOnCluster(Cluster mqCluster, TopicConfig topicConfig) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<?>>() {
            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                long start = System.currentTimeMillis();
                Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(mqAdmin, mqCluster.getName());
                if (masterSet.size() == 0) {
                    logger.error("create or update topic failed:no master node, cluser:{}, topic:{}", mqCluster, topicConfig);
                    return Result.getResult(Status.BROKER_NOT_EXIST_ERROR);
                }
                for (String addr : masterSet) {
                    mqAdmin.createAndUpdateTopicConfig(addr, topicConfig);
                }
                long end = System.currentTimeMillis();
                logger.info("create or update topic use:{}ms,topic:{}", (end- start), topicConfig.getTopicName());
                return Result.getOKResult();
            }
            @Override
            public Result<?> exception(Exception e) throws Exception {
                logger.error("create or update topic:{} err:{}", topicConfig.getTopicName(), e.getMessage());
                return Result.getWebErrorResult(e);
            }
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }
    
    /**
     * 更新topic
     * @param mqCluster
     * @param auditTopic
     */
    @Transactional
    public Result<?> updateTopic(Topic topic) {
        try {
            // 第一步：更新topic记录
            Topic dbTopic = new Topic();
            dbTopic.setId(topic.getId());
            dbTopic.setQueueNum(topic.getQueueNum());
            Integer count = topicDao.update(dbTopic);
            if(count == null || count != 1) {
                return Result.getResult(Status.DB_ERROR);
            }
            // 第二步：真实更新topic
            AuditTopic auditTopic = new AuditTopic();
            BeanUtils.copyProperties(topic, auditTopic);
            Cluster mqCluster = clusterService.getMQClusterById(topic.getClusterId());
            Result<?> result = createAndUpdateTopicOnCluster(mqCluster, auditTopic);
            if(result.isNotOK()) {
                throw new RuntimeException("update topic:"+auditTopic.getName()+" on cluster err!");
            }
        } catch (Exception e) {
            logger.error("updateTopic topic:{}", topic, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getWebErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 删除topic
     * @param mqCluster
     * @param auditTopic
     */
    @Transactional
    public Result<?> deleteTopic(Topic topic) {
        try {
            // 第一步：删除topic记录
            Integer count = topicDao.delete(topic.getId());
            if(count == null || count != 1) {
                return Result.getResult(Status.DB_ERROR);
            }
            // 第二步：删除producer
            Integer deleteCount = userProducerService.delete(topic.getId());
            if(deleteCount == null) {
                return Result.getResult(Status.DB_ERROR);
            }
            // 第三步：真实删除topic(为了防止误删，只有线上环境才能删除)
            Cluster mqCluster = clusterService.getMQClusterById(topic.getClusterId());
            Result<?> result = deleteTopicOnCluster(mqCluster, topic.getName());
            if(result.isNotOK()) {
                throw new RuntimeException("delete topic:"+topic.getName()+" on cluster err!");
            }
            // 第四步：清理关联表
            userFootprintService.deleteByTid(topic.getId());
            userFavoriteService.deleteByTid(topic.getId());
        } catch (Exception e) {
            logger.error("deleteTopic topic:{}", topic, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getWebErrorResult(e);
        }
        return Result.getOKResult();
    }
    
    /**
     * 删除topic
     * @param mqCluster
     * @param auditTopic
     */
    public Result<?> deleteTopicOnCluster(Cluster mqCluster, String topic) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<?>>() {
            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                long start = System.currentTimeMillis();
                Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(mqAdmin, mqCluster.getName());
                mqAdmin.deleteTopicInBroker(masterSet, topic);
                long end = System.currentTimeMillis();
                logger.info("delete topic use:{}ms,topic:{},broker:{}", (end- start), topic, masterSet);
                mqAdmin.deleteTopicInNameServer(null, topic);
                logger.info("delete topic use:{}ms,topic:{} in namesrv", (System.currentTimeMillis() - end), topic);
                return Result.getOKResult();
            }
            @Override
            public Result<?> exception(Exception e) throws Exception {
                logger.error("delete topic:{} err:{}", topic, e.getMessage());
                return Result.getWebErrorResult(e);
            }
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
    }
    
    
    /**
     * 按照name查询topic
     * 
     * @param Result<List<Topic>>
     */
    public Result<List<Topic>> queryTopicListByNameList(Collection<String> nameCollection) {
        List<Topic> topicList = null;
        try {
            topicList = topicDao.selectByNameList(nameCollection);
        } catch (Exception e) {
            logger.error("queryTopicListByNameList err, nameCollection:{}", nameCollection, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicList);
    }
    
    /**
     * 查询所有topic consumer
     * 
     * @param Result<List<TopicConsumer>>
     */
    public Result<List<TopicConsumer>> queryTopicConsumer(int consumeWay) {
        List<TopicConsumer> topicConsumerList = null;
        try {
            topicConsumerList = topicDao.selectTopicConsumer(consumeWay);
        } catch (Exception e) {
            logger.error("queryTopicConsumer err", e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicConsumerList);
    }

    /**
     * 查询指定topic对应的consumer
     *
     */
    public Result<List<TopicConsumer>> queryTopicConsumer(long tid) {
        List<TopicConsumer> topicConsumerList = null;
        try {
            topicConsumerList = topicDao.selectTopicConsumerByTid(tid);
        } catch (Exception e) {
            logger.error("queryTopicConsumer err,tid:{}", tid, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(topicConsumerList);
    }
    
    /**
     * 初始化topic（从集群导入到数据库中），可以执行多次，因为数据库有唯一索引
     * 该方法适用于公司内部已经搭建了mq集群，想使用mqcloud进行管理
     * @param cluster
     * @return  topic状态
     */
    @SuppressWarnings("rawtypes")
    public Result<?> initTopic(Cluster cluster){
        Result<?> result = mqAdminTemplate.execute(new MQAdminCallback<Result<?>>() {
            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                // 获取broker集群信息
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                // 获取一个master地址
                String brokerAddr = clusterInfo.getBrokerAddrTable().entrySet().iterator().next().getValue()
                        .getBrokerAddrs().get(0L);
                // 获取所有topic配置
                TopicConfigSerializeWrapper allTopicConfig = mqAdmin.getAllTopicConfig(brokerAddr, 5000);
                // 获取所有topic
                TopicList topicList = mqAdmin.fetchAllTopicList();
                // 获取系统topic
                TopicList systemTopicList = ((SohuMQAdmin) mqAdmin).getSystemTopicList(10 * 1000);
                    
                List<Result> resultList = new ArrayList<Result>();
                for(String topic : topicList.getTopicList()) {
                    // topic过滤
                    if (CommonUtil.isRetryTopic(topic)
                            || CommonUtil.isDeadTopic(topic)
                            || systemTopicList.getTopicList().contains(topic)) {
                        resultList.add(Result.getResult(Status.FILTERED_TOPIC).setResult(topic));
                        continue;
                    }
                    // 获取路由信息
                    Topic topicObject = new Topic();
                    topicObject.setClusterId(cluster.getId());
                    topicObject.setName(topic);
                    TopicConfig topicConfig = allTopicConfig.getTopicConfigTable().get(topic);
                    if(topicConfig != null) {
                        topicObject.setOrdered(topicConfig.isOrder() ? 1 : 0);
                        topicObject.setQueueNum(topicConfig.getReadQueueNums());
                        try {
                            save(topicObject);
                            resultList.add(Result.getResult(topic));
                        } catch (Exception e) {
                            logger.error("save topic:{}", topicObject, e);
                            resultList.add(Result.getWebErrorResult(e).setResult(topic).setException(e));
                        }
                    } else {
                        logger.error("topic:{} no config", topic);
                        resultList.add(Result.getResult(Status.NO_CONFIG_TOPIC).setResult(topic));
                    }
                }
                return Result.getResult(resultList);
            }
            public Cluster mqCluster() {
                return cluster;
            }
            @Override
            public Result<?> exception(Exception e) throws Exception {
                logger.error("cluster:{} error", cluster, e);
                return Result.getWebErrorResult(e).setException(e);
            }
        });
        return result;
    }
    
    
    /**
     * 更新topic流量
     * @param topicTrafficList
     * @return
     */
    public Result<Integer> updateCount(List<TopicTraffic> topicTrafficList) {
        Integer result = null;
        try {
            result = topicDao.updateCount(topicTrafficList);
        } catch (Exception e) {
            logger.error("updateCount err, topicTrafficList:{}", topicTrafficList, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 更新topic
     * @return
     */
    public Result<Integer> updateDBTopic(Topic topic) {
        Integer result = null;
        try {
            result = topicDao.update(topic);
        } catch (Exception e) {
            logger.error("updateDBTopic err, topic:{}", topic, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 重置topic流量
     * @param topicTrafficList
     * @return
     */
    public Result<Integer> resetCount(int dayAgo) {
        Integer result = null;
        try {
            Date dt = new Date(System.currentTimeMillis() - dayAgo * 24 * 60 * 60 * 1000);
            dt = DateUtil.parseYMD(DateUtil.formatYMD(dt));
            result = topicDao.resetCount(dt);
        } catch (Exception e) {
            logger.error("resetCount err, dayAgo:{}", dayAgo, e);
            return Result.getDBErrorResult(e);
        }
        return Result.getResult(result);
    }
    
    /**
     * 创建 trace topic
     * 
     * @param audit
     * @param auditTopic
     * @param traceClusterId
     * @return
     */
    public Result<?> createTraceTopic(Audit audit, AuditTopic auditTopic, Integer traceClusterId) {
        if (traceClusterId == null) {
            return Result.getResult(Status.TRACE_CLUSTER_ID_IS_NULL);
        }
        // 获取集群
        Cluster mqCluster = clusterService.getMQClusterById(traceClusterId);
        if (mqCluster == null) {
            return Result.getResult(Status.TRACE_CLUSTER_IS_NULL);
        }
        // 更改topic名称 例如mqcloud-test-topic转化成mqcloud-test-trace-topic
        auditTopic.setName(CommonUtil.buildTraceTopic(auditTopic.getName()));
        auditTopic.setProducer(CommonUtil.buildTraceTopicProducer(auditTopic.getName()));
        auditTopic.setTraceEnabled(0);
        // 创建topic
        Result<?> createResult = createTopic(mqCluster, audit, auditTopic);
        if (createResult.isNotOK()) {
            logger.error("create trace topic err ! traceTopic:{}", auditTopic.getName());
            return Result.getResult(Status.TRACE_TOPIC_CREATE_ERROR);
        }
        return Result.getOKResult();
    }
    
    /**
     * 更新topic
     * @param mqCluster
     * @param auditTopic
     */
    @Transactional
    public Result<?> updateTopicTrace(Audit audit, Topic topic, int traceClusterId) {
        try {
            // 第一步：更新topic记录
            Integer count = topicDao.updateTopicTrace(topic.getId(), topic.getTraceEnabled());
            if(count == null || count != 1) {
                return Result.getResult(Status.DB_ERROR);
            }
            if(topic.traceEnabled()) {
                Result<Topic> topicResult = queryTopic(CommonUtil.buildTraceTopic(topic.getName()));
                // trace topic已经存在，没必要创建
                if(topicResult.isOK()) {
                    return Result.getOKResult();
                }
                // 第二步：真实更新topic
                AuditTopic auditTopic = new AuditTopic();
                auditTopic.setName(topic.getName());
                auditTopic.setQueueNum(8);
                return createTraceTopic(audit, auditTopic, traceClusterId);
            }
        } catch (Exception e) {
            logger.error("updateTopicTrace topic:{}", topic, e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.getWebErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 更新topic流量预警
     * @param audit
     * @param topic
     */
    public Result<?> updateTopicTrafficWarn(Topic topic) {
        try {
            Integer count = topicDao.updateTopicTrafficWarn(topic.getId(), topic.getTrafficWarnEnabled());
            if(count == null || count != 1) {
                return Result.getResult(Status.DB_ERROR);
            }
        } catch (Exception e) {
            logger.error("updateTopicTrafficWarn topic:{}", topic, e);
            return Result.getWebErrorResult(e);
        }
        return Result.getOKResult();
    }

    /**
     * 获取http方式消费的topic和消费者
     * @return
     */
    public Result<List<TopicConsumer>> queryHttpConsumer() {
        try {
            return Result.getResult(topicDao.selectHttpTopicConsumer());
        } catch (Exception e) {
            logger.error("selectHttpTopicConsumer err", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 获取http方式消费的topic和消费者
     * @return
     */
    public Result<List<TopicProducer>> queryHttpProducer() {
        try {
            return Result.getResult(topicDao.selectHttpTopicProducer());
        } catch (Exception e) {
            logger.error("selectHttpTopicProducer err", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 获取topic各个队列状态数据
     *
     * @param mqCluster
     * @param topic
     */
    public TopicStatsTable stats(Cluster cluster, String brokerAddr, String topic) {
        return mqAdminTemplate.execute(new MQAdminCallback<TopicStatsTable>() {
            public TopicStatsTable callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                return sohuMQAdmin.getMQClientInstance().getMQClientAPIImpl().getTopicStatsInfo(brokerAddr, topic,
                        5000);
            }

            public Cluster mqCluster() {
                return cluster;
            }

            @Override
            public TopicStatsTable exception(Exception e) throws Exception {
                logger.warn("brokerAddr:{}, topic:{}, err:{}", brokerAddr, topic, e.toString());
                return null;
            }
        });
    }
}

