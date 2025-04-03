package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.dao.NameServerDao;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import org.apache.rocketmq.common.namesrv.NamesrvUtil;
import org.apache.rocketmq.remoting.RemotingClient;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.header.namesrv.PutKVConfigRequestHeader;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * name server
 *
 * @author yongfeigao
 * @date 2018年10月23日
 */
@Service
public class NameServerService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NameServerDao nameServerDao;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private MQDeployer mqDeployer;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private ConsumerClientStatService consumerClientStatService;

    @Autowired
    private ProducerTotalStatService producerTotalStatService;

    /**
     * 保存记录
     *
     * @return 返回Result
     */
    public Result<?> save(int cid, String addr) {
        return save(cid, addr, null);
    }

    /**
     * 保存记录
     *
     * @return 返回Result
     */
    public Result<?> save(int cid, String addr, String baseDir) {
        try {
            Result result = Result.getResult(nameServerDao.insert(cid, addr, baseDir));
            if (result.isOK()) {
                initOrderTopicConfig(cid, addr);
            }
            return result;
        } catch (Exception e) {
            logger.error("insert err, cid:{}, addr:{}, baseDir:{}", cid, addr, baseDir, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 初始化order topic配置
     *
     * @param cid
     * @param addr
     */
    private void initOrderTopicConfig(int cid, String addr) {
        String kvConfig = mqCloudConfigHelper.getOrderTopicKVConfig(String.valueOf(cid));
        if (kvConfig == null || kvConfig.isEmpty()) {
            return;
        }
        Result<List<String>> listResult = topicService.queryOrderedTopicList(cid);
        if (listResult.isEmpty()) {
            return;
        }
        mqAdminTemplate.execute(new MQAdminCallback<Void>() {
            public Void callback(MQAdminExt mqAdmin) throws Exception {
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                RemotingClient remotingClient = sohuMQAdmin.getMQClientInstance().getMQClientAPIImpl().getRemotingClient();
                List<String> topics = listResult.getResult();
                for (String topic : topics) {
                    PutKVConfigRequestHeader requestHeader = new PutKVConfigRequestHeader();
                    requestHeader.setNamespace(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG);
                    requestHeader.setKey(topic);
                    requestHeader.setValue(kvConfig);
                    RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.PUT_KV_CONFIG, requestHeader);
                    remotingClient.invokeSync(addr, request, 5000);
                }
                return null;
            }

            public Void exception(Exception e) throws Exception {
                throw e;
            }

            public Cluster mqCluster() {
                return clusterService.getMQClusterById(cid);
            }
        });
    }

    /**
     * 查询集群的name server
     *
     * @return Result<List < NameServer>>
     */
    public Result<List<NameServer>> query(int cid) {
        try {
            return Result.getResult(nameServerDao.selectByClusterId(cid));
        } catch (Exception e) {
            logger.error("query cid:{} err", cid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询集群的name server
     */
    public Result<List<NameServer>> queryOK(int cid) {
        try {
            return Result.getResult(nameServerDao.selectOKByClusterId(cid));
        } catch (Exception e) {
            logger.error("queryOK:{} err", cid, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询name server
     *
     * @return Result<NameServer>
     */
    public Result<NameServer> query(String addr) {
        try {
            return Result.getResult(nameServerDao.selectByAddr(addr));
        } catch (Exception e) {
            logger.error("query addr:{} err", addr, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 查询全部name server
     *
     * @return Result<List < NameServer>>
     */
    public Result<List<NameServer>> queryAll() {
        try {
            return Result.getResult(nameServerDao.selectAll());
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
    public Result<?> delete(int cid, String addr) {
        try {
            return Result.getResult(nameServerDao.delete(cid, addr));
        } catch (Exception e) {
            logger.error("delete err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
    }


    /**
     * 更新记录
     *
     * @param cid
     * @param addr
     * @return
     */
    public Result<?> update(int cid, String addr, CheckStatusEnum checkStatusEnum) {
        try {
            return Result.getResult(nameServerDao.update(cid, addr, checkStatusEnum.getStatus()));
        } catch (Exception e) {
            logger.error("update err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 更新状态
     *
     * @param cid
     * @param addr
     * @param status
     * @return
     */
    public Result<?> updateStatus(int cid, String addr, int status) {
        try {
            return Result.getResult(nameServerDao.updateStatus(cid, addr, status));
        } catch (Exception e) {
            logger.error("updateStatus err, cid:{}, addr:{}", cid, addr, e);
            return Result.getDBErrorResult(e);
        }
    }

    /**
     * 健康检查
     *
     * @param cluster
     * @param addr
     * @return
     */
    public Result<?> healthCheck(Cluster cluster, String addr) {
        return mqAdminTemplate.execute(new MQAdminCallback<Result<?>>() {
            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                try {
                    mqAdmin.getNameServerConfig(Arrays.asList(addr));
                    return Result.getOKResult();
                } catch (Exception e) {
                    return Result.getDBErrorResult(e).setMessage("addr:" + addr + ";Exception: " + e.getMessage());
                }
            }

            public Cluster mqCluster() {
                return cluster;
            }

            @Override
            public Result<?> exception(Exception e) throws Exception {
                return Result.getDBErrorResult(e).setMessage("Exception: " + e.getMessage());
            }
        });
    }

    /**
     * 获取连接信息
     */
    public Result<?> getConnectionAddress(int cid, String ip, int port) {
        // 获取连接信息
        Result<List<String>> result = mqDeployer.getConnectionAddress(ip, port);
        if (result.isEmpty()) {
            return result;
        }
        // 构建连接信息
        List<ComponentConnection> connections = buildComponentConnection(cid, ip, result.getResult());
        // 为没有链接名的连接填充消费者
        fillConsumerConnectionName(connections);
        // 为没有链接名的连接填充生产者
        fillProducerConnectionName(connections);
        return Result.getResult(connections);
    }

    /**
     * 构建连接信息
     */
    private List<ComponentConnection> buildComponentConnection(int cid, String ip, List<String> addrs) {
        // 获取broker和proxy信息
        Result<List<Broker>> brokerListResult = brokerService.query(cid);
        Result<List<Proxy>> proxyListResult = proxyService.query(cid);
        return addrs.stream().map(addr -> {
            ComponentConnection connection = new ComponentConnection();
            connection.setAddr(addr);
            String connectionIp = connection.getIp();
            // 检测是否为broker链接
            Optional<Broker> brokerOptional = filter(brokerListResult, connectionIp);
            brokerOptional.ifPresent(broker -> {
                connection.setName(broker.getBrokerName());
            });
            // 检测是否为proxy链接
            Optional<Proxy> proxyOptional = filter(proxyListResult, connectionIp);
            proxyOptional.ifPresent(proxy -> {
                connection.setName("proxy");
            });
            if (ip.equals(connectionIp)) {
                connection.setName("self");
            }
            if (mqCloudConfigHelper.getMqcloudServers().contains(connectionIp)) {
                connection.setName("mqcloud");
            }
            return connection;
        }).sorted().collect(Collectors.toList());
    }

    /**
     * 过滤ip
     */
    private <T extends DeployableComponent> Optional<T> filter(Result<List<T>> listResult, String ip) {
        if (listResult.isEmpty()) {
            return Optional.empty();
        }
        return listResult.getResult().stream()
                .filter(component -> component.getIp().equals(ip))
                .findFirst();
    }

    /**
     * 填充消费者连接名
     */
    private void fillConsumerConnectionName(List<ComponentConnection> connections) {
        // 为没有链接名的连接填充消费者
        Set<String> noNameConns = getNoNameConns(connections);
        if (noNameConns.isEmpty()) {
            return;
        }
        List<ConsumerClientStat> list = consumerClientStatService.selectByDateAndClient(new Date(), noNameConns).getResult();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, String> map = list.stream().collect(
                Collectors.toMap(ConsumerClientStat::getClient, ConsumerClientStat::getConsumer));
        setConnectionName(connections, map);
    }

    /**
     * 填充生产者连接名
     */
    private void fillProducerConnectionName(List<ComponentConnection> connections) {
        // 为没有链接名的连接填充生产者
        Set<String> noNameConns = getNoNameConns(connections);
        if (noNameConns.isEmpty()) {
            return;
        }
        List<ProducerTotalStat> list = producerTotalStatService.queryByDateAndIp(new Date(), noNameConns).getResult();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, String> map = list.stream().collect(
                Collectors.toMap(ProducerTotalStat::getIp, ProducerTotalStat::getProducer));
        setConnectionName(connections, map);
    }

    /**
     * 获取没有名字的链接
     */
    private Set<String> getNoNameConns(List<ComponentConnection> connections) {
        return connections.stream()
                .filter(connection -> connection.getName() == null)
                .map(ComponentConnection::getIp)
                .collect(Collectors.toSet());
    }

    /**
     * 设置连接名
     */
    private void setConnectionName(List<ComponentConnection> connections, Map<String, String> map) {
        if (CollectionUtils.isEmpty(map)) {
            return;
        }
        connections.stream()
                .filter(conn -> conn.getName() == null)
                .forEach(conn -> {
                    conn.setName(map.get(conn.getIp()));
                });
    }
}
