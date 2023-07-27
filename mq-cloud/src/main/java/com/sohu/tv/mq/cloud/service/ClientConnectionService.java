package com.sohu.tv.mq.cloud.service;

import com.google.common.collect.Sets;
import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.dao.*;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.ManagerParam;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.vo.ClientLanguageVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.remoting.protocol.body.Connection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.apache.rocketmq.remoting.protocol.LanguageCode;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: 客户端链接信息
 * @date 2022/4/26 17:38
 */
@Service
public class ClientConnectionService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private TopicDao topicDao;

    @Resource
    private UserProducerDao userProducerDao;

    @Resource
    private UserConsumerDao userConsumerDao;

    @Resource
    private UserDao userDao;

    @Resource
    private ClientLanguageDao clientLanguageDao;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private ClusterService clusterService;

    /**
     * @description: 获取所有客户端连接及语言版本信息
     * @param: * @param:
     * @return: void
     * @author fengwang219475
     * @date: 2022/5/5 14:20
     */
    public void scanAllClientGroupConnectLanguage(String topicName) {

        try {

            if(StringUtils.isBlank(topicName)){
                logger.warn("topicName is null ,the task will scanning all client,the start time :{}", System.currentTimeMillis());
            }

            List<Topic> needScanProducerList = topicDao.selectAllWithProducer(topicName);

            if (!CollectionUtils.isEmpty(needScanProducerList)) {
                for (Topic topicWithProducer : needScanProducerList) {
                    Cluster cluster = clusterService.getMQClusterById(topicWithProducer.getClusterId());
                    if (cluster == null) {
                        continue;
                    }
                    Result<ProducerConnection> producerConnectionResult = mqAdminTemplate.execute(new MQAdminCallback<Result<ProducerConnection>>() {
                        @Override
                        public Result<ProducerConnection> callback(MQAdminExt mqAdmin) throws Exception {
                            ProducerConnection producerConnection = mqAdmin.examineProducerConnectionInfo(topicWithProducer.getProducerName(), topicWithProducer.getName());
                            return Result.getResult(producerConnection);
                        }

                        @Override
                        public Result<ProducerConnection> exception(Exception e) throws Exception {
                            logger.warn("Failed to get {} connection, skip this producer,the error is {}", topicWithProducer.getProducerName(), e.getMessage());
                            return Result.getErrorResult(Status.WEB_ERROR, e);
                        }

                        @Override
                        public Cluster mqCluster() {
                            return cluster;
                        }
                    });
                    // 构建对象并插入
                    if (producerConnectionResult.isOK()) {
                        ProducerConnection result = producerConnectionResult.getResult();
                        buildAndSaveResult(result.getConnectionSet(), topicWithProducer.getProducerName(), topicWithProducer.getId(),
                                ClientLanguage.PRODUCER_CLIENT_GROUP_TYPE, cluster);
                    }
                }
            }

            List<Topic> needScanConsumerList = topicDao.selectAllWithConsumer(topicName);

            if (!CollectionUtils.isEmpty(needScanConsumerList)) {
                for (Topic topicWithConsumer : needScanConsumerList) {
                    Cluster cluster = clusterService.getMQClusterById(topicWithConsumer.getClusterId());
                    if (cluster == null) {
                        continue;
                    }
                    Result<ConsumerConnection> consumerConnectionResult = mqAdminTemplate.execute(new MQAdminCallback<Result<ConsumerConnection>>() {
                        @Override
                        public Result<ConsumerConnection> callback(MQAdminExt mqAdmin) throws Exception {
                            ConsumerConnection consumerConnection = mqAdmin.examineConsumerConnectionInfo(topicWithConsumer.getConsumerName());
                            return Result.getResult(consumerConnection);
                        }

                        @Override
                        public Result<ConsumerConnection> exception(Exception e) throws Exception {
                            logger.warn("Failed to get {} connection, skip this consumer,the error is {}", topicWithConsumer.getConsumerName(), e.getMessage());
                            return Result.getErrorResult(Status.WEB_ERROR, e);
                        }

                        @Override
                        public Cluster mqCluster() {
                            return cluster;
                        }
                    });
                    // 构建对象并插入
                    if (consumerConnectionResult.isOK()) {
                        ConsumerConnection result = consumerConnectionResult.getResult();
                        buildAndSaveResult(result.getConnectionSet(), topicWithConsumer.getConsumerName(), topicWithConsumer.getId()
                                , ClientLanguage.CONSUMER_CLIENT_GROUP_TYPE, cluster);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("scanning all producer have error, the task thead is interrupt", e);
        }

        logger.info("time:{} scanning all connection finished", new Date());
    }

    /**
     * @description: 封装connnection信息并插入
     * @param: * @param: connectionSet 链接集合，只取第一个
     * @param: groupClientName 生产消费组名称
     * @param: tid topic ID
     * @param: groupType 客户端类型，取自 com.sohu.tv.mq.cloud.bo.ClientLanguage#ConsumerClientGroupType
     * @param: cluster 集群
     * @return: void
     * @author fengwang219475
     * @date: 2022/5/5 14:21
     */
    private void buildAndSaveResult(Set<Connection> connectionSet, String groupClientName, Long tid, Integer groupType,
                                    Cluster cluster) {
        try {
            Connection connection = connectionSet.stream().findFirst().get();
            // 首先判断下是否是消费端，如果是，非Java客户端采用消费端运行信息获取版本号
            if (groupType == ClientLanguage.CONSUMER_CLIENT_GROUP_TYPE) {
                HashSet<Connection> connectionHashSet = fixNonJavaClientVersion(Sets.newHashSet(connection),
                        groupClientName, cluster);
                if (!CollectionUtils.isEmpty(connectionHashSet)) {
                    connection = connectionHashSet.stream().findFirst().get();
                }
            }
            ClientLanguage clientLanguage = new ClientLanguage();
            LanguageCode[] values = LanguageCode.values();
            for (LanguageCode value : values) {
                if (connection.getLanguage() == value) {
                    clientLanguage.setLanguage(value.getCode());
                    if (connection instanceof VersionConnection) {
                        VersionConnection versionConnection = (VersionConnection) connection;
                        clientLanguage.setVersion(StringUtils.upperCase(versionConnection.getVersionStr()));
                    } else {
                        clientLanguage.setVersion(MQVersion.getVersionDesc(connection.getVersion()));
                    }
                    break;
                }
            }
            clientLanguage.setTid(tid);
            clientLanguage.setCid((long) cluster.getId());
            clientLanguage.setClientGroupName(groupClientName);
            Date now = new Date();
            clientLanguage.setCreateDate(now);
            clientLanguage.setUpdateTime(now);
            clientLanguage.setClientGroupType(groupType);
            List<Integer> uidList = new ArrayList<>(1);
            if (groupType == ClientLanguage.PRODUCER_CLIENT_GROUP_TYPE) {
                uidList = userProducerDao.selectUidByProduceName(groupClientName);
            } else {
                uidList = userConsumerDao.selectUidByConsumerName(groupClientName);
            }
            if (uidList.isEmpty()) {
                return;
            }
            String uids = uidList.stream().map(String::valueOf).collect(Collectors.joining(","));
            clientLanguage.setRelationUids(uids);
            clientLanguageDao.insert(clientLanguage);
        } catch (Exception e) {
            logger.error("save {} data to database errr,the cause of the error is {}", groupClientName, e);
        }
    }

    /**
     * @description: 解析非Java消费客户端链接
     * @param: * @param: connectionSet
     * @param: consumerGroup
     * @param: cluster
     * @return: java.util.HashSet<org.apache.rocketmq.remoting.protocol.body.Connection>
     * @author fengwang219475
     * @date: 2022/5/5 10:17
     */
    public HashSet<Connection> fixNonJavaClientVersion(HashSet<Connection> connectionSet, String consumerGroup, Cluster cluster) {
        // 如果解析出现问题，直接返回原链接
        try {
            if (CollectionUtils.isEmpty(connectionSet)) {
                return connectionSet;
            }

            Iterator<Connection> iterator = connectionSet.iterator();
            HashSet newParseConsumerCon = new HashSet();
            while (iterator.hasNext()) {
                Connection node = iterator.next();
                if (LanguageCode.JAVA.equals(node.getLanguage())) {
                    continue;
                }
                // 删除该节点链接，重新解析
                iterator.remove();
                Connection parseConnection = mqAdminTemplate.execute(new MQAdminCallback<Connection>() {
                    @Override
                    public Connection callback(MQAdminExt mqAdmin) throws Exception {
                        String clientId = node.getClientId();
                        ConsumerRunningInfo consumerRunningInfo = mqAdmin.getConsumerRunningInfo(consumerGroup, clientId,
                                false);
                        Connection connection = parseDiffClientVersion(consumerRunningInfo, node);
                        return connection;
                    }

                    @Override
                    public Connection exception(Exception e) throws Exception {
                        logger.warn("fetch consumer running data error,the error is {}", e.getMessage());
                        return node;
                    }

                    @Override
                    public Cluster mqCluster() {
                        return cluster;
                    }
                });
                newParseConsumerCon.add(parseConnection);
            }
            // 重新加入新解析的链接
            connectionSet.addAll(newParseConsumerCon);

        } catch (Exception e) {
            logger.error("clientLanguage#fixNonJavaClientVersion() parse consumer connection error", e);
        }
        return connectionSet;
    }

    /**
     * @description: 从消费信息中获取版本信息
     * 当前获取的版本信息存在以下问题：
     * 1. go 从消费端运行信息中获取版本信息，但版本号只体现大版本，小版本忽略，例如2.1.1和2.1.0在go版本中都只体现为2.1.0
     * 2. python python客户端完全基于cpp运行的，只是做了一层简单封装，无自身的语言信息和版本信息，所以无法判断python客户端
     * 3. cpp 也是从消费端运行信息中获取，但获取版本信息存在于编译信息中，需要解析长字符串，这个如果后期格式发生变化，就会出问题
     * 其他版本信息暂时没有获取到
     * @param: * @param: consumerRunningInfo
     * @param: connection
     * @return: org.apache.rocketmq.remoting.protocol.body.Connection
     * @author fengwang219475
     * @date: 2022/5/5 10:18
     */
    private Connection parseDiffClientVersion(ConsumerRunningInfo consumerRunningInfo, Connection connection) {
        try {
            Properties properties = consumerRunningInfo.getProperties();
            LanguageCode language = connection.getLanguage();
            String version = "";
            switch (language) {
                case GO:
                    version = Optional.ofNullable(properties.get("PROP_CLIENT_VERSION"))
                            .map(String::valueOf).orElse("");
                    break;
                case CPP:
                    String versionParseStr = Optional.ofNullable(properties.get("PROP_CLIENT_CORE_VERSION"))
                            .map(String::valueOf).orElse("");
                    if (StringUtils.isNotBlank(versionParseStr)) {
                        // 纯字符串解析，变数较多，后续需要进行优化
                        versionParseStr = versionParseStr.split(",")[0];
                        version = versionParseStr.substring(versionParseStr.indexOf(":") + 1);
                    }
                    break;
                default:
                    version = MQVersion.getVersionDesc(connection.getVersion());
                    break;
            }
            return new VersionConnection(connection, version);
        } catch (Exception e) {
            logger.error("clientLanguage#parseDiffClientVersion() is error", e);
            return connection;
        }
    }

    /**
     * @description: 主要检查表中是否更新了客户端版本，以人工为准
     * @param: * @param: connectionSet
     * @param: consumerGroup
     * @param: mqCluster
     * @return: java.util.HashSet<org.apache.rocketmq.remoting.protocol.body.Connection>
     * @author fengwang219475
     * @date: 2022/5/13 16:33
     */
    public HashSet<Connection> checkConnectVersion(HashSet<Connection> connectionSet, String consumerGroup,
                                                   Integer clientType, long cid) {
        try {
            ClientLanguage queryParams = new ClientLanguage();
            queryParams.setCid(cid);
            queryParams.setClientGroupType(clientType);
            queryParams.setClientGroupName(consumerGroup);
            queryParams.setModifyType(ClientLanguage.MODIFYBYPERSON);
            List<ClientLanguage> clientLanguages = clientLanguageDao.selectByParams(queryParams, 0, 1);
            if (CollectionUtils.isEmpty(clientLanguages)) {
                return connectionSet;
            }
            String version = clientLanguages.get(0).getVersion();
            HashSet<Connection> newConnection = new HashSet<>(connectionSet.size());
            for (Connection connection : connectionSet) {
                VersionConnection versionConnection = new VersionConnection(connection, version);
                newConnection.add(versionConnection);
            }
            return newConnection;
        } catch (Exception e) {
            logger.error("clientLanguage#checkConnectVersion() is error", e);
            return connectionSet;
        }
    }

    /**
     * @description: 多条件查询
     * @param: * @param: param
     * @param: paginationParam
     * @return: com.sohu.tv.mq.cloud.util.Result<java.util.List < com.sohu.tv.mq.cloud.web.vo.ClientLanguageVo>>
     * @author fengwang219475
     * @date: 2022/5/7 18:15
     */
    public Result<List<ClientLanguageVo>> queryByConditional(ManagerParam param, PaginationParam paginationParam) {
        try {
            ClientLanguage queryParams = new ClientLanguage();
            queryParams.setCid(param.getCid());
            if (StringUtils.isNotBlank(param.getLanguage())) {
                queryParams.setLanguage(Integer.valueOf(param.getLanguage()).byteValue());
            }
            if (StringUtils.isNotBlank(param.getGroupName())){
                queryParams.setClientGroupName(param.getGroupName());
            }
            Integer count = clientLanguageDao.selectCountByParams(queryParams);
            if (count == null || count == 0){
                return Result.getOKResult();
            }
            paginationParam.caculatePagination(count);
            List<ClientLanguage> clientLanguages = clientLanguageDao.selectByParams(queryParams, paginationParam.getBegin(),
                    paginationParam.getNumOfPage());
            if (CollectionUtils.isEmpty(clientLanguages)) {
                return Result.getOKResult();
            }

            // 映射 key:tid-> value:cid
            Map<Long, Long> tidToCid = clientLanguages.stream()
                    .collect(Collectors.toMap(ClientLanguage::getTid, ClientLanguage::getCid, (k1, k2) -> k1));

            // 获取Topic
            Set<Long> tids = tidToCid.keySet();
            List<Topic> topicList = topicDao.selectByIdList(tids);
            if (CollectionUtils.isEmpty(topicList)) {
                return Result.getOKResult();
            }

            // 转换为Map，方便获取
            Map<Long, List<Topic>> topicMap = topicList.stream().collect(Collectors.groupingBy(Topic::getId));

            // 封装返回对象
            Set<Long> uidList = new HashSet<>();
            List<ClientLanguageVo> resultList = new ArrayList<>(paginationParam.getNumOfPage());
            int index = paginationParam.getBegin();
            for (int i = 0; i < clientLanguages.size(); i++) {
                ClientLanguage clientLanguage = clientLanguages.get(i);
                ClientLanguageVo clientLanguageVo = new ClientLanguageVo();
                clientLanguageVo.setClientLanguage(clientLanguages.get(i));
                clientLanguageVo.setIndex(++index);
                clientLanguageVo.setTopic(Optional.ofNullable(topicMap.get(clientLanguage.getTid()))
                        .map(node -> node.get(0))
                        .orElse(null));
                clientLanguageVo.setClusterName(Optional.ofNullable(clusterService.getMQClusterById(clientLanguageVo.getClientLanguage().getCid()))
                        .map(Cluster::getName).orElse(null));
                resultList.add(clientLanguageVo);
                clientLanguageVo.castUidStrToList();
                uidList.addAll(clientLanguageVo.getUids());
            }

            // 查询人员
            List<User> users = userDao.selectByIdList(uidList);
            if (CollectionUtils.isEmpty(users)) {
                return Result.getResult(resultList);
            }
            // 装填关联人员信息
            Map<Long, List<User>> userMap = users.stream().collect(Collectors.groupingBy(User::getId));
            for (ClientLanguageVo clientLanguageVo : resultList) {
                for (Long uid : clientLanguageVo.getUids()) {
                    Optional.ofNullable(userMap.get(uid))
                            .map(nodeList -> nodeList.get(0))
                            .ifPresent(userNode -> clientLanguageVo.getRelationUsers().add(userNode));
                }
                clientLanguageVo.initAttributes();
            }
            return Result.getResult(resultList);
        } catch (Exception e) {
            logger.error("clientLanguage#queryByCondition() is error,the detail message: ", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * @description: 查询现已注册的客户端语言
     * @param: * @param:
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/5/5 16:40
     */
    public Result<?> selectAllLanguage() {
        try {
            List<AdminLanguage> languageCodes = clientLanguageDao.selectAllLanguage()
                    .stream()
                    .sorted()
                    .map(node -> new AdminLanguage(LanguageCode.valueOf(node)))
                    .collect(Collectors.toList());
            return Result.getResult(languageCodes);
        } catch (Exception e) {
            logger.error("clientLanguage#selectAllLanguage() is error,the detail message is :", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * @description: 手动更新
     * @param: * @param: clientLanguage
     * @return: void
     * @author fengwang219475
     * @date: 2022/5/6 16:10
     */
    public Result<?> updateClientData(ClientLanguage clientLanguage) {
        if (StringUtils.isBlank(clientLanguage.getClientGroupName())) {
            return Result.getOKResult();
        }
        try {
            clientLanguageDao.update(clientLanguage);
            return Result.getOKResult();
        } catch (Exception e) {
            logger.error("clientLanguage#updateClientData() is error,the detail message is :", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * @description: 查询单条记录
     * @param: * @param: clientLanguage
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/5/11 20:40
     */
    public Result<?> query(ClientLanguage clientLanguage, boolean withLanguagesList) {
        try {
            List<ClientLanguage> clientLanguages = clientLanguageDao.selectByParams(clientLanguage, 0, 1);
            if (!CollectionUtils.isEmpty(clientLanguages)) {
                ClientLanguageVo clientLanguageVo = new ClientLanguageVo();
                clientLanguageVo.setClientLanguage(clientLanguages.get(0));
                clientLanguageVo.initAttributes();
                if (withLanguagesList) {
                    List<AdminLanguage> adminLanguages = (List<AdminLanguage>) selectAllLanguage().getResult();
                    clientLanguageVo.setLanguageList(adminLanguages);
                }
                return Result.getResult(clientLanguageVo);
            }
            return Result.getOKResult();
        } catch (Exception e) {
            logger.error("clientLanguage#query() is error,the detail message is :", e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * @description: 获取已扫描的所有客户端名称
     * @param: * @param:
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/5/26 10:17
     */
    public Result<?> selectgetAllGroupName() {
        try {
            List<String> groupNames = clientLanguageDao.selectgetAllGroupName();
            return Result.getResult(groupNames);
        } catch (Exception e) {
            logger.error("clientLanguage#selectgetAllGroupName() is error,the detail message is :", e);
            return Result.getDBErrorResult(e);
        }
    }
}



