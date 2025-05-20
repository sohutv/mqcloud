package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.common.model.BrokerRateLimitData;
import com.sohu.tv.mq.cloud.common.model.UpdateSendMsgRateLimitRequestHeader;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.*;
import com.sohu.tv.mq.cloud.web.controller.param.BrokerConfigParam;
import com.sohu.tv.mq.cloud.web.controller.param.BrokerConfigUpdateParam;
import com.sohu.tv.mq.cloud.web.controller.param.DataMigrationParam;
import com.sohu.tv.mq.cloud.web.controller.param.UpdateSendMsgRateLimitParam;
import com.sohu.tv.mq.cloud.web.vo.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.common.MQVersion.Version;
import org.apache.rocketmq.common.constant.PermName;
import org.apache.rocketmq.common.running.RunningStats;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.function.Function;

/**
 * broker
 * 
 * @author yongfeigao
 * @date 2018年11月14日
 */
@Controller
@RequestMapping("/admin/broker")
public class AdminBrokerController extends AdminViewController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private BrokerConfigGroupService brokerConfigGroupService;

    @Autowired
    private BrokerConfigService brokerConfigService;

    @Autowired
    private ClusterConfigService clusterConfigService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private DataMigrationService dataMigrationService;

    @Autowired
    private ClusterBrokerAutoUpdateService clusterBrokerAutoUpdateService;

    @Autowired
    private BrokerAutoUpdateService brokerAutoUpdateService;

    @Autowired
    private BrokerAutoUpdateStepService brokerAutoUpdateStepService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private ConsumerService consumerService;

    @RequestMapping("/list")
    public String list(@RequestParam(name = "cid", required = false) Integer cid, Map<String, Object> map) {
        setView(map, "list");
        Cluster cluster = clusterService.getOrDefaultMQCluster(cid);
        if (cluster == null) {
            return view();
        }
        cluster = clusterService.queryById(cluster.getId()).getResult();
        Result<List<Broker>> brokerListResult = null;
        // 从数据库查询当前集群的broker列表
        brokerListResult = brokerService.query(cluster.getId());
        // 数据库不存在broker列表时，从nameserver拉取
        if (brokerListResult.isNotOK()) {
            brokerListResult = getBrokerListFromNameServer(cluster);
        }
        Map<String, List<BrokerStatVO>> brokerGroup = null;
        if (brokerListResult.isOK()) {
            List<Broker> brokerList = brokerListResult.getResult();
            brokerGroup = fetchBrokerRuntimeStats(brokerList, cluster);
        }
        // 生成vo
        ClusterInfoVO clusterInfoVO = new ClusterInfoVO();
        // 发生异常
        if (brokerGroup != null) {
            clusterInfoVO.setHasNameServer(true);
        }
        if (brokerGroup != null && brokerGroup.size() > 0) {
            clusterInfoVO.setBrokerGroup(brokerGroup);
            // 查询临时broker
            Result<List<Broker>> brokerTmpList = brokerService.queryTmpBroker(cluster.getId());
            if (brokerTmpList.isNotEmpty()) {
                for (Broker broker : brokerTmpList.getResult()) {
                    BrokerStatVO brokerStatVO = new BrokerStatVO();
                    brokerStatVO.setBrokerAddr(broker.getAddr());
                    brokerStatVO.setBrokerId(String.valueOf(broker.getBrokerID()));
                    brokerStatVO.setBaseDir(broker.getBaseDir());
                    brokerStatVO.setCreateTime(broker.getCreateTime());
                    brokerStatVO.setTmp(true);
                    brokerGroup.computeIfAbsent(broker.getBrokerName(), k -> new ArrayList<>()).add(brokerStatVO);
                }
            }
            // 设置slave落后量
            for (List<BrokerStatVO> brokerStatVOList : brokerGroup.values()) {
                brokerStatVOList.stream().filter(BrokerStatVO::isMaster).findAny().ifPresent(master -> {
                    brokerStatVOList.stream()
                            .filter(stat -> !stat.isMaster())
                            .forEach(stat -> stat.setFallbehindSize(master.getCommitLogMaxOffset() - stat.getCommitLogMaxOffset()));
                });
                // 排序
                Collections.sort(brokerStatVOList, (o1, o2) -> {
                    if (o1.getBrokerId().equals(o2.getBrokerId())) {
                        return o1.getCreateTime().compareTo(o2.getCreateTime());
                    }
                    return o1.getBrokerId().compareTo(o2.getBrokerId());
                });
            }
        }
        clusterInfoVO.setMqCluster(clusterService.getAllMQCluster());
        clusterInfoVO.setSelectedMQCluster(cluster);
        setResult(map, clusterInfoVO);
        setResult(map, "username", mqCloudConfigHelper.getServerUser());
        // 查询延时消息用
        MessageQueryCondition messageQueryCondition = new MessageQueryCondition();
        messageQueryCondition.setCid(cluster.getId());
        messageQueryCondition.setTopic("SCHEDULE_TOPIC_XXXX");
        setResult(map, "messageQueryCondition", messageQueryCondition);
        setResult(map, "mqcloudDomain", mqCloudConfigHelper.getDomain());
        return view();
    }

    /**
     * 从nameserver 拉取当前集群的broker地址
     *
     * @param mqCluster
     * @return
     */
    private Result<List<Broker>> getBrokerListFromNameServer(Cluster mqCluster) {
        Result<List<Broker>> brokerListResult = mqAdminTemplate.execute(new MQAdminCallback<Result<List<Broker>>>() {
            public Result<List<Broker>> callback(MQAdminExt mqAdmin) throws Exception {
                // 获取集群信息
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                // 获得broker地址map
                Map<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
                List<Broker> list = new ArrayList<Broker>();
                // 遍历集群中所有的broker
                for (String brokerName : brokerAddrTable.keySet()) {
                    HashMap<Long, String> brokerAddrs = brokerAddrTable.get(brokerName).getBrokerAddrs();
                    for (Long brokerId : brokerAddrs.keySet()) {
                        Broker broker = new Broker();
                        broker.setAddr(brokerAddrs.get(brokerId));
                        broker.setBrokerID(brokerId.intValue());
                        broker.setBrokerName(brokerName);
                        list.add(broker);
                    }
                }
                return Result.getResult(list);
            }

            public Cluster mqCluster() {
                return mqCluster;
            }

            public Result<List<Broker>> exception(Exception e) throws Exception {
                logger.error("cluster:{} err", mqCluster(), e);
                return Result.getWebErrorResult(e);
            }
        });
        return brokerListResult;
    }

    /**
     * list接口过长，拆分，获取brokerGroupMap
     *
     * @param brokerList
     * @param mqCluster
     * @return
     */
    private Map<String, List<BrokerStatVO>> fetchBrokerRuntimeStats(List<Broker> brokerList, Cluster mqCluster) {
        Map<String, List<BrokerStatVO>> brokerGroup = new TreeMap<>();
        for (Broker broker : brokerList) {
            // 公共逻辑 拼接BrokerStatVO
            BrokerStatVO brokerStatVO = new BrokerStatVO();
            brokerStatVO.setBrokerAddr(broker.getAddr());
            brokerStatVO.setBaseDir(broker.getBaseDir());
            brokerStatVO.setBrokerId(String.valueOf(broker.getBrokerID()));
            brokerStatVO.setCreateTime(broker.getCreateTime());
            brokerGroup.computeIfAbsent(broker.getBrokerName(), k -> new ArrayList<>()).add(brokerStatVO);
            // 监控结果
            brokerStatVO.setCheckStatus(broker.getCheckStatus());
            brokerStatVO.setCheckTime(broker.getCheckTimeFormat());
            brokerStatVO.setWritable(broker.isWritable());
            // 当有broker down时，数据库中的broker 地址已过时，增加异常处理
            Result<KVTable> kvTableResult = brokerService.fetchBrokerRuntimeStats(broker.getAddr(), mqCluster);
            KVTable kvTable = kvTableResult.getResult();
            if (kvTable != null) {
                // 处理broker stats 数据
                handleBrokerStat(broker, kvTable, brokerStatVO);
                brokerStatVO.setCheckStatus(CheckStatusEnum.OK.getStatus());
            } else {
                brokerStatVO.setCheckStatus(CheckStatusEnum.FAIL.getStatus());
            }
        }
        return brokerGroup;
    }

    /**
     * 处理broker stat数据
     *
     * @param brokerStatMap
     * @param kvTable
     * @param broker
     */
    private void handleBrokerStat(Broker broker, KVTable kvTable, BrokerStatVO brokerStatVO) {
        HashMap<String, String> stats = kvTable.getTable();
        // 启动时间 转换
        String bootTime = stats.get("bootTimestamp");
        String boot = DateUtil.getFormat(DateUtil.YMD_BLANK_HMS_COLON).format(new Date(NumberUtils.toLong(bootTime)));
        stats.put("bootTimestamp", boot);
        // 版本
        brokerStatVO.setVersion(removeFromMap(stats, "brokerVersionDesc"));
        kvTable.getTable().remove("brokerVersion");
        // 流量
        brokerStatVO.setInTps(formatTraffic(removeFromMap(stats, "putTps")));
        String getTransferedTps = removeFromMap(stats, "getTransferedTps");
        if (getTransferedTps == null) {
            getTransferedTps = removeFromMap(stats, "getTransferredTps");
        }
        brokerStatVO.setOutTps(formatTraffic(getTransferedTps));
        // 延迟队列
        for (MessageDelayLevel messageDelayLevel : MessageDelayLevel.values()) {
            String offsetString = removeFromMap(stats,
                    RunningStats.scheduleMessageOffset.name() + "_" + messageDelayLevel.getLevel());
            if (offsetString == null) {
                continue;
            }
            String[] offsets = offsetString.split(",");
            if (offsets != null && offsets.length == 2) {
                long curOffset = NumberUtils.toLong(offsets[0]);
                long maxOffset = NumberUtils.toLong(offsets[1]);
                brokerStatVO.addDelayMessageOffset(messageDelayLevel, curOffset, maxOffset);
            }
        }
        brokerStatVO.setCommitLogMaxOffset(NumberUtils.toLong(removeFromMap(stats, "commitLogMaxOffset")));
        // 定时消息指标
        brokerStatVO.setTimerCongestNum(NumberUtils.toLong(removeFromMap(stats, "timerCongestNum")));
        brokerStatVO.setTimerEnqueueTps(NumberUtils.toFloat(removeFromMap(stats, "timerEnqueueTps")));
        brokerStatVO.setTimerDequeueTps(NumberUtils.toFloat(removeFromMap(stats, "timerDequeueTps")));
        brokerStatVO.setTimerOffsetBehind(NumberUtils.toLong(removeFromMap(stats, "timerOffsetBehind")));
        brokerStatVO.setTimerReadBehind(NumberUtils.toLong(removeFromMap(stats, "timerReadBehind")));
        // 其余指标
        brokerStatVO.setInfo(new TreeMap<String, String>(stats));

        // 客户端连接数
        brokerStatVO.setProducerSize(removeFromMap(stats, "producerSize"));
        brokerStatVO.setConsumerSize(removeFromMap(stats, "consumerSize"));
        brokerStatVO.setProducerConnectionSize(removeFromMap(stats, "producerConnectionSize"));
        brokerStatVO.setConsumerConnectionSize(removeFromMap(stats, "consumerConnectionSize"));

        // 不包含系统topic的生产消费量
        String putStats = removeFromMap(stats, "brokerPutStatsFromExternal");
        if (putStats != null) {
            String[] putStatsArray = putStats.split(" ");
            if (putStatsArray.length == 2) {
                brokerStatVO.setInCountWithoutSystemTopic(putStatsArray[1]);
            }
        }
        String getStats = removeFromMap(stats, "brokerGetStatsWithoutSystemTopic");
        if (getStats != null) {
            String[] getStatsArray = getStats.split(" ");
            if (getStatsArray.length == 2) {
                brokerStatVO.setOutCountWithoutSystemTopic(getStatsArray[1]);
            }
        }
    }

    private String removeFromMap(HashMap<String, String> map, String key) {
        return map.remove(key);
    }

    private String formatTraffic(String value) {
        String[] array = value.split(" ");
        if (array != null && array.length > 0) {
            return String.format("%.2f", NumberUtils.toDouble(array[0]));
        }
        return "-";
    }

    /**
     * 禁止写入
     *
     * @param cid
     * @param broker
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/nowrite", method = RequestMethod.POST)
    public Result<?> nowrite(UserInfo ui, @RequestParam(name = "cid") Integer cid,
                             @RequestParam(name = "addr") String addr) {
        Cluster mqCluster = clusterService.getOrDefaultMQCluster(cid);
        logger.warn("nowrite {}:{}, user:{}", mqCluster, addr, ui);
        Result<Broker> brokerResult = brokerService.queryBroker(cid, addr);
        if (brokerResult.isNotOK()) {
            return Result.getWebResult(brokerResult);
        }
        Broker broker = brokerResult.getResult();
        return Result.getWebResult(brokerService.wipeWritePerm(cid, broker.getBrokerName(), broker.getAddr()));
    }

    /**
     * 恢复写入
     *
     * @param cid
     * @param broker
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/resume/write", method = RequestMethod.POST)
    public Result<?> resumeWrite(UserInfo ui, @RequestParam(name = "cid") Integer cid,
                                 @RequestParam(name = "addr") String addr) {
        Cluster mqCluster = clusterService.getOrDefaultMQCluster(cid);
        logger.warn("resumeWrite {}:{}, user:{}", mqCluster, addr, ui);
        Result<Broker> brokerResult = brokerService.queryBroker(cid, addr);
        if (brokerResult.isNotOK()) {
            return Result.getWebResult(brokerResult);
        }
        Broker broker = brokerResult.getResult();
        return brokerService.addWritePerm(broker);
    }

    /**
     * 流量展示
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/traffic")
    public String traffic(UserInfo userInfo, @RequestParam("ip") String ip, Map<String, Object> map)
            throws Exception {
        return adminViewModule() + "/traffic";
    }

    /**
     * 初始化topic
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/init/topic", method = RequestMethod.POST)
    public String initTopic(UserInfo userInfo, @RequestParam("cid") Integer cid,
                            @RequestParam(name = "broker", required = false) String broker, Map<String, Object> map)
            throws Exception {
        Cluster cluster = clusterService.getMQClusterById(cid);
        Result<Broker> brokerAddressResult = (Result<Broker>) chooseBroker(cid, broker);
        if (brokerAddressResult.isNotOK()) {
            setResult(map, brokerAddressResult);
            return adminViewModule() + "/initTopic";
        }
        Result<?> result = topicService.initTopic(cluster, brokerAddressResult.getResult().getAddr());
        setResult(map, result);
        return adminViewModule() + "/initTopic";
    }

    /**
     * 初始化consumer
     *
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/init/consumer", method = RequestMethod.POST)
    public String initConsumer(UserInfo userInfo, @RequestParam("cid") Integer cid,
                               @RequestParam(name = "broker", required = false) String broker, Map<String, Object> map)
            throws Exception {
        Cluster cluster = clusterService.getMQClusterById(cid);
        Result<List<Topic>> topicListResult = topicService.queryTopicList(cluster);
        if (topicListResult.isEmpty()) {
            setResult(map, Result.getWebResult(topicListResult));
            return adminViewModule() + "/initConsumer";
        }
        Result<Broker> brokerAddressResult = (Result<Broker>) chooseBroker(cid, broker);
        if (brokerAddressResult.isNotOK()) {
            setResult(map, brokerAddressResult);
            return adminViewModule() + "/initConsumer";
        }
        Result<?> result = topicService.initTopic(cluster, brokerAddressResult.getResult().getAddr());

        Map<String, List<Result>> resultMap = consumerService.initConsumer(cluster, topicListResult.getResult(),
                brokerAddressResult.getResult().getAddr());
        setResult(map, Result.getResult(resultMap));
        return adminViewModule() + "/initConsumer";
    }

    private Result<?> chooseBroker(int cid, String brokerAddr) {
        // 先处理空白字符
        if (brokerAddr != null) {
            brokerAddr = brokerAddr.trim();
            if (brokerAddr.length() == 0) {
                brokerAddr = null;
            }
        }
        if (brokerAddr == null) {
            Result<List<Broker>> result = brokerService.query(cid);
            if (result.isEmpty()) {
                return result;
            }
            return result.getResult().stream()
                    .filter(b->b.isMaster())
                    .findFirst()
                    .map(b->Result.getResult(b))
                    .orElse(Result.getResult(Status.NO_RESULT));
        } else {
            return brokerService.queryBroker(cid, brokerAddr);
        }
    }

    /**
     * broker列表
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/bklist", method = RequestMethod.GET)
    public Result<?> brokerList(int cid, Map<String, Object> map) throws Exception {
        return brokerService.query(cid);
    }

    @ResponseBody
    @RequestMapping(value = "/_refresh", method = RequestMethod.POST)
    public Result<?> _refresh(UserInfo ui, @RequestParam(name = "cid") int cid) {
        return refresh(ui, cid);
    }

    /**
     * 刷新
     * 
     * @param cid
     * @param broker
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/refresh", method = RequestMethod.POST)
    public Result<?> refresh(UserInfo ui, @RequestParam(name = "cid") int cid) {
        logger.info("refresh broker info cid =" + cid);
        Result<List<Broker>> brokerListResult = mqAdminTemplate.execute(new MQAdminCallback<Result<List<Broker>>>() {
            public Result<List<Broker>> callback(MQAdminExt mqAdmin) throws Exception {
                // 获取集群信息
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                // 获得broker地址map
                Map<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
                if (brokerAddrTable.isEmpty()) {
                    return Result.getResult(Status.NO_RESULT);
                }
                List<Broker> list = new ArrayList<Broker>();
                // 遍历集群中所有的broker
                for (String brokerName : brokerAddrTable.keySet()) {
                    HashMap<Long, String> brokerAddrs = brokerAddrTable.get(brokerName).getBrokerAddrs();
                    TopicRouteData topicRouteData = mqAdmin.examineTopicRouteInfo(brokerName);
                    boolean writable = true;
                    if (topicRouteData != null && !CollectionUtils.isEmpty(topicRouteData.getQueueDatas())) {
                        writable = topicRouteData.getQueueDatas().stream()
                                .filter(q -> brokerName.equals(q.getBrokerName()))
                                .findFirst()
                                .map(q -> PermName.isWriteable(q.getPerm()))
                                .orElse(true);
                    }
                    for (Long brokerId : brokerAddrs.keySet()) {
                        Broker broker = new Broker();
                        broker.setBrokerName(brokerName);
                        broker.setAddr(brokerAddrs.get(brokerId));
                        broker.setBrokerID(brokerId.intValue());
                        broker.setCid(cid);
                        broker.setWritable(writable);
                        list.add(broker);
                        Properties properties = mqAdmin.getBrokerConfig(broker.getAddr());
                        String rocketmqHome = properties.getProperty("rocketmqHome");
                        broker.setBaseDir(rocketmqHome);
                        // 更新集群数据文件保留时间
                        clusterService.updateFileReservedTime(properties, cid);
                        // 获取broker版本
                        Result kvTableResult = brokerService.fetchBrokerRuntimeStats(broker.getAddr(), mqCluster());
                        RocketMQVersion rocketMQVersion = RocketMQVersion.V5;
                        if (kvTableResult.getResult() != null) {
                            Map<String, String> map = ((KVTable) kvTableResult.getResult()).getTable();
                            String versionOrdinalString = map.get("brokerVersion");
                            int versionOrdinal = NumberUtils.toInt(versionOrdinalString, -1);
                            if (versionOrdinal != -1 && versionOrdinal < Version.V5_0_0.ordinal()) {
                                rocketMQVersion = RocketMQVersion.V4;
                            }
                        }
                        broker.setVersion(rocketMQVersion.getVersion());
                    }
                }
                return Result.getResult(list);
            }

            public Result<List<Broker>> exception(Exception e) throws Exception {
                logger.error("examineBroker cid:{}", cid, e);
                return Result.getWebErrorResult(e);
            }

            public Cluster mqCluster() {
                return clusterService.getOrDefaultMQCluster(cid);
            }
        });
        if (brokerListResult.isEmpty()) {
            return brokerListResult;
        }
        Result<?> result = brokerService.refresh(cid, brokerListResult.getResult());
        return Result.getWebResult(result);
    }

    /**
     * 获取配置
     * 
     * @return
     */
    @RequestMapping(value = "/config")
    public String config(UserInfo ui, @RequestParam(name = "cid", required = false) Integer cid,
            Map<String, Object> map) {
        setView(map, "config");
        // 获取所有broker配置
        Result<List<BrokerConfigGroup>> brokerConfigGroupResult = brokerConfigGroupService.query();
        Result<List<BrokerConfig>> brokerConfigResult = brokerConfigService.query();
        List<BrokerConfigGroup> brokerConfigGroupList = brokerConfigGroupResult.getResult();
        List<BrokerConfig> brokerConfigList = brokerConfigResult.getResult();
        List<BrokerConfigGroupVO> brokerConfigGroupVOList = toBrokerConfigGroupVOList(brokerConfigGroupList,
                brokerConfigList);
        setResult(map, brokerConfigGroupVOList);
        // 获取集群
        Cluster[] clusters = clusterService.getAllMQCluster();
        if (clusters != null && clusters.length > 0) {
            Cluster selectCluster = null;
            if (cid != null) {
                selectCluster = clusterService.getMQClusterById(cid);
            }
            if (selectCluster == null) {
                selectCluster = clusters[0];
            }
            setResult(map, "selectCluster", selectCluster);
            setResult(map, "clusters", clusters);
            // 获取集群配置
            Result<List<ClusterConfig>> clusterConfigListResult = clusterConfigService.query(selectCluster.getId());
            List<ClusterConfig> clusterConfigList = clusterConfigListResult.getResult();
            List<BrokerConfigGroupVO> list = toBrokerConfigGroupVOList(clusterConfigList);
            if (list != null) {
                setResult(map, "clusterConfigs", list);
                // 标识broker配置是否可选
                if (brokerConfigList != null && clusterConfigList != null) {
                    for (BrokerConfig brokerConfig : brokerConfigList) {
                        for (ClusterConfig clusterConfig : clusterConfigList) {
                            if (brokerConfig.getId() == clusterConfig.getBid()) {
                                brokerConfig.setCanSelect(false);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return view();
    }

    /**
     * 转换
     * 
     * @param brokerConfigGroupList
     * @param brokerConfigList
     * @return
     */
    private List<BrokerConfigGroupVO> toBrokerConfigGroupVOList(List<BrokerConfigGroup> brokerConfigGroupList,
            List<BrokerConfig> brokerConfigList) {
        if (brokerConfigGroupList == null || brokerConfigGroupList.size() == 0) {
            return null;
        }
        List<BrokerConfigGroupVO> list = new ArrayList<>();
        for (BrokerConfigGroup brokerConfigGroup : brokerConfigGroupList) {
            BrokerConfigGroupVO brokerConfigGroupVO = new BrokerConfigGroupVO(brokerConfigGroup);
            list.add(brokerConfigGroupVO);
            if (brokerConfigList == null) {
                continue;
            }
            for (BrokerConfig brokerConfig : brokerConfigList) {
                if (brokerConfigGroup.getId() == brokerConfig.getGid()) {
                    brokerConfigGroupVO.add(brokerConfig);
                }
            }
        }
        return list;
    }

    /**
     * 添加配置组
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/add/config/group")
    public Result<?> addConfigGroup(UserInfo ui, @RequestParam(name = "group") String group,
            @RequestParam(name = "order") int order, Map<String, Object> map) {
        group = group.trim();
        if (group.length() == 0) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        return Result.getWebResult(brokerConfigGroupService.save(group, order));
    }

    /**
     * 更新配置组
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/update/config/group")
    public Result<?> updateConfigGroup(UserInfo ui, @RequestParam(name = "id") int id,
            @RequestParam(name = "group") String group,
            @RequestParam(name = "order") int order, Map<String, Object> map) {
        BrokerConfigGroup brokerConfigGroup = new BrokerConfigGroup();
        brokerConfigGroup.setId(id);
        brokerConfigGroup.setGroup(group);
        brokerConfigGroup.setOrder(order);
        return Result.getWebResult(brokerConfigGroupService.update(brokerConfigGroup));
    }

    /**
     * 删除配置组
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delete/config/group")
    public Result<?> deleteConfigGroup(UserInfo ui, @RequestParam(name = "id") int id, Map<String, Object> map) {
        Result<List<BrokerConfig>> result = brokerConfigService.query(id);
        if (result.isNotEmpty()) {
            return Result.getResult(Status.DELETE_BROKER_CONFIG_FIRST);
        }
        return Result.getWebResult(brokerConfigGroupService.delete(id));
    }

    /**
     * 获取配置
     * 
     * @return
     */
    @RequestMapping(value = "/online/config")
    public String onlineConfig(UserInfo ui, @RequestParam(name = "cid") int cid,
            @RequestParam(name = "addr") String addr, Map<String, Object> map) {
        Result<Properties> result = brokerService.fetchBrokerConfig(cid, addr);
        if (result.isNotOK()) {
            setResult(map, result);
            return adminViewModule() + "/onlineConfig";
        }
        setResult(map, toBrokerConfigVO(result.getResult()));
        return adminViewModule() + "/onlineConfig";
    }

    /**
     * 添加配置
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/add/config")
    public Result<?> addConfig(UserInfo ui, @Valid BrokerConfigParam brokerConfigParam, Map<String, Object> map) {
        BrokerConfig brokerConfig = new BrokerConfig();
        BeanUtils.copyProperties(brokerConfigParam, brokerConfig);
        Result<Integer> result = brokerConfigService.save(brokerConfig);
        return Result.getWebResult(result);
    }

    /**
     * 删除配置
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delete/config")
    public Result<?> deleteConfig(UserInfo ui, @RequestParam(name = "id") int id, Map<String, Object> map) {
        return Result.getWebResult(brokerConfigService.delete(id));
    }

    /**
     * 更新配置
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/update/config")
    public Result<?> updateConfig(UserInfo ui, @Valid BrokerConfigParam brokerConfigParam, Map<String, Object> map) {
        BrokerConfig brokerConfig = new BrokerConfig();
        BeanUtils.copyProperties(brokerConfigParam, brokerConfig);
        return Result.getWebResult(brokerConfigService.update(brokerConfig));
    }

    /**
     * 属性解析为对象
     * 
     * @param properties
     * @return
     */
    private BrokerConfigVO toBrokerConfigVO(Properties properties) {
        // 获取所有的broker和分组默认配置
        List<BrokerConfigGroup> brokerConfigGroupList = brokerConfigGroupService.query().getResult();
        List<BrokerConfig> brokerConfigList = brokerConfigService.query().getResult();

        BrokerConfigVO brokerConfigVO = new BrokerConfigVO();
        Map<Integer, BrokerConfigGroupVO> groupMap = new HashMap<>();
        for (Object keyObj : properties.keySet()) {
            String key = keyObj.toString();
            String value = properties.getProperty(key);
            // 获取broker配置
            BrokerConfig brokerConfig = find(brokerConfigList, config -> {
                return key.equals(config.getKey());
            });
            if (brokerConfig != null) {
                brokerConfig.setOnlineValue(value);
                addGroup(groupMap, brokerConfigGroupList, brokerConfig);
            } else {
                brokerConfigVO.addUnknowItem(key, value);
            }
        }
        brokerConfigVO.setBrokerConfigGroups(sortBrokerConfigGroupVOColletion(groupMap.values()));
        return brokerConfigVO;
    }

    /**
     * 更新线上配置
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/update/online/config")
    public Result<?> updateOnlineConfig(UserInfo ui, BrokerConfigUpdateParam brokerConfigUpdateParam) {
        logger.info("user:{} modify brokerConfigUpdateParam:{}", ui, brokerConfigUpdateParam);
        return Result.getWebResult(brokerService.updateBrokerConfig(brokerConfigUpdateParam));
    }

    /**
     * 添加集群配置
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/add/cluster/config")
    public Result<?> addClusterConfig(UserInfo ui, @RequestParam(name = "cid") int cid,
            @RequestParam(name = "bids") String bids,
            Map<String, Object> map) {
        String[] bidArray = bids.split(",");
        for (String bid : bidArray) {
            Result<BrokerConfig> brokerConfigResult = brokerConfigService.queryById(NumberUtils.toInt(bid));
            if (brokerConfigResult.isNotOK()) {
                logger.warn("bid:{} not exist", bid);
                return Result.getWebResult(brokerConfigResult);
            }
            BrokerConfig brokerConfig = brokerConfigResult.getResult();
            ClusterConfig clusterConfig = new ClusterConfig();
            clusterConfig.setBid(brokerConfig.getId());
            clusterConfig.setOnlineValue(brokerConfig.getValue());
            clusterConfig.setCid(cid);
            Result<?> saveResult = clusterConfigService.save(clusterConfig);
            if (saveResult.isNotOK()) {
                logger.warn("bid:{} save err", bid);
                return Result.getWebResult(saveResult);
            }
        }
        return Result.getOKResult();
    }

    /**
     * 更新集群配置
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/update/cluster/config")
    public Result<?> updateClusterConfig(UserInfo ui, @RequestParam(name = "cid") int cid,
            @RequestParam(name = "bid") int bid,
            @RequestParam(name = "onlineValue") String onlineValue, Map<String, Object> map) {
        ClusterConfig clusterConfig = new ClusterConfig();
        clusterConfig.setBid(bid);
        clusterConfig.setOnlineValue(onlineValue);
        clusterConfig.setCid(cid);
        return Result.getWebResult(clusterConfigService.update(clusterConfig));
    }

    /**
     * 删除集群配置
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delete/cluster/config")
    public Result<?> deleteClusterConfig(UserInfo ui, @RequestParam(name = "cid") int cid,
            @RequestParam(name = "bid") int bid, Map<String, Object> map) {
        return Result.getWebResult(clusterConfigService.delete(cid, bid));
    }

    /**
     * 配置选择
     * 
     * @return
     */
    @RequestMapping(value = "/cluster/config")
    public String clusterConfig(UserInfo ui, @RequestParam(name = "cid") int cid, Map<String, Object> map) {
        Result<List<ClusterConfig>> result = clusterConfigService.query(cid);
        setResult(map, toBrokerConfigGroupVOList(result.getResult()));
        return adminViewModule() + "/clusterConfig";
    }

    /**
     * 配置选择
     *
     * @return
     */
    @RequestMapping(value = "/ratelimit/info")
    public String rateLimitInfo(UserInfo ui, @RequestParam(name = "cid") int cid,
                                @RequestParam(name = "addr") String addr, Map<String, Object> map) {
        Result<BrokerRateLimitData> result = brokerService.fetchSendMessageRateLimitInBroker(cid, addr);
        setResult(map, result);
        setResult(map, "configHelper", mqCloudConfigHelper);
        return adminViewModule() + "/rateLimitInfo";
    }

    /**
     * 限流更新
     * @param ui
     * @param cid
     * @param addr
     * @param updateSendMsgRateLimitParam
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/ratelimit/update")
    public Result<?> rateLimitUpdate(UserInfo ui, @RequestParam(name = "cid") int cid, @RequestParam(name = "addr") String addr,
                                     UpdateSendMsgRateLimitParam updateSendMsgRateLimitParam) {
        UpdateSendMsgRateLimitRequestHeader updateSendMsgRateLimitRequestHeader =
                new UpdateSendMsgRateLimitRequestHeader();
        BeanUtils.copyProperties(updateSendMsgRateLimitParam, updateSendMsgRateLimitRequestHeader);
        Result<?> result = brokerService.updateSendMessageRateLimit(cid, addr, updateSendMsgRateLimitRequestHeader);
        return Result.getWebResult(result);
    }

    @RequestMapping(value = "/timerWheel/_metrics")
    public String _timerWheelMetrics(UserInfo ui, @RequestParam(name = "cid") int cid,
                                    @RequestParam(name = "addr") String addr, Map<String, Object> map) {
        return timerWheelMetrics(ui, cid, addr, map);
    }

    /**
     * 时间轮指标
     * @param ui
     * @param cid
     * @param addr
     * @return
     */
    @RequestMapping(value = "/timerWheel/metrics")
    public String timerWheelMetrics(UserInfo ui, @RequestParam(name = "cid") int cid,
                                    @RequestParam(name = "addr") String addr, Map<String, Object> map) {
        Result<?> result = brokerService.getTimerWheelMetrics(cid, addr);
        setResult(map, result);
        return adminViewModule() + "/timerWheelMetrics";
    }

    /**
     * 迁移列表
     *
     * @return
     */
    @GetMapping(value = "/migration/list")
    public String migrationList(UserInfo ui, Map<String, Object> map) {
        setView(map, "migration/list");
        Result<?> result = dataMigrationService.queryAllDataMigration();
        setResult(map, result);
        setResult(map, "module", mqCloudConfigHelper.getRsyncModule());
        return view();
    }

    /**
     * 添加迁移任务
     *
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/migration/add")
    public Result<?> addMigration(UserInfo ui, @Valid DataMigrationParam dataMigrationParam) {
        DataMigration dataMigration = new DataMigration();
        BeanUtils.copyProperties(dataMigrationParam, dataMigration);
        Result<?> result = dataMigrationService.addDataMigration(dataMigration);
        return result;
    }

    /**
     * 重新运行任务
     *
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/migration/rerun")
    public Result<?> rerunMigration(UserInfo ui, long id) {
        return dataMigrationService.rerunDataMigration(id);
    }

    /**
     * 空运行，用于提示
     *
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/migration/dry/run")
    public Result<?> dryRunMigration(UserInfo ui, @Valid DataMigrationParam dataMigrationParam) {
        DataMigration dataMigration = new DataMigration();
        BeanUtils.copyProperties(dataMigrationParam, dataMigration);
        Result<?> result = dataMigrationService.dryRunDataMigrationTask(dataMigration);
        return result;
    }

    /**
     * 监控日志
     */
    @ResponseBody
    @GetMapping(value = "/migration/monitor/log")
    public PageLog monitorLog(UserInfo ui, int id, String ip, int offset, int size) {
        return dataMigrationService.tailLog(id, ip, offset, size);
    }

    /**
     * producer连接
     */
    @GetMapping(value = "/producer/connection")
    public String producerConnection(@RequestParam(name = "cid") int cid,
                                     @RequestParam(name = "addr") String addr,
                                     Map<String, Object> map) {
        setResult(map, brokerService.fetchAllProducerConnection(addr, clusterService.getMQClusterById(cid)));
        return adminViewModule() + "/clientConnectionInfo";
    }

    /**
     * consumer连接
     */
    @GetMapping(value = "/consumer/connection")
    public String consumerConnection(@RequestParam(name = "cid") int cid,
                                     @RequestParam(name = "addr") String addr,
                                     Map<String, Object> map) {
        setResult(map, brokerService.fetchAllConsumerConnection(addr, clusterService.getMQClusterById(cid)));
        return adminViewModule() + "/clientConnectionInfo";
    }

    /**
     * 属性解析为对象
     * 
     * @param properties
     * @return
     */
    private List<BrokerConfigGroupVO> toBrokerConfigGroupVOList(List<ClusterConfig> clusterConfigList) {
        if (clusterConfigList == null || clusterConfigList.size() <= 0) {
            return null;
        }
        // 获取所有的broker和分组默认配置
        List<BrokerConfigGroup> brokerConfigGroupList = brokerConfigGroupService.query().getResult();
        List<BrokerConfig> brokerConfigList = brokerConfigService.query().getResult();
        Map<Integer, BrokerConfigGroupVO> groupMap = new HashMap<>();
        for (ClusterConfig clusterConfig : clusterConfigList) {
            // 获取broker配置
            BrokerConfig brokerConfig = find(brokerConfigList, config -> {
                return config.getId() == clusterConfig.getBid();
            });
            if (brokerConfig != null) {
                brokerConfig.setOnlineValue(clusterConfig.getOnlineValue());
                addGroup(groupMap, brokerConfigGroupList, brokerConfig);
            }
        }
        return sortBrokerConfigGroupVOColletion(groupMap.values());
    }

    /**
     * 将brokerconfig加到组中
     * 
     * @param groupMap
     * @param brokerConfigGroupList
     * @param brokerConfig
     */
    private void addGroup(Map<Integer, BrokerConfigGroupVO> groupMap, List<BrokerConfigGroup> brokerConfigGroupList,
            BrokerConfig brokerConfig) {
        // 获取broker config group
        BrokerConfigGroupVO brokerConfigGroupVO = groupMap.get(brokerConfig.getGid());
        if (brokerConfigGroupVO == null) {
            brokerConfigGroupVO = new BrokerConfigGroupVO(find(brokerConfigGroupList, brokerConfigGroup -> {
                return brokerConfig.getGid() == brokerConfigGroup.getId();
            }));
            groupMap.put(brokerConfig.getGid(), brokerConfigGroupVO);
        }
        brokerConfigGroupVO.add(brokerConfig);
    }

    private <T> T find(List<T> list, Function<T, Boolean> function) {
        if (list == null || list.size() == 0) {
            return null;
        }
        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            T t = iterator.next();
            Boolean exist = function.apply(t);
            if (exist) {
                iterator.remove();
                return t;
            }
        }
        return null;
    }

    /**
     * 排序
     * 
     * @param values
     * @return
     */
    private List<BrokerConfigGroupVO> sortBrokerConfigGroupVOColletion(Collection<BrokerConfigGroupVO> values) {
        if (values.size() <= 0) {
            return null;
        }
        // brokerConfig 排序
        List<BrokerConfigGroupVO> list = new ArrayList<>(values);
        for (BrokerConfigGroupVO brokerConfigGroupVO : list) {
            Collections.sort(brokerConfigGroupVO.getBrokerConfigList(), (o1, o2) -> {
                return o1.getOrder() - o2.getOrder();
            });
        }
        // brokerConfigGroup 排序
        Collections.sort(list, (o1, o2) -> {
            return o1.getBrokerConfigGroup().getOrder() - o2.getBrokerConfigGroup().getOrder();
        });
        return list;
    }

    /**
     * broker自动更新列表
     */
    @GetMapping(value = "/autoUpdate/list")
    public String autoUpdateList(@RequestParam(name = "cid") int cid, Map<String, Object> map) {
        setResult(map, brokerAutoUpdateService.selectByCid(cid));
        return adminViewModule() + "/autoUpdate/list";
    }

    /**
     * 增加broker自动更新
     */
    @ResponseBody
    @PostMapping(value = "/autoUpdate/add")
    public Result<?> autoUpdateAdd(@RequestParam(name = "cid") int cid, @RequestParam(name = "action") int action) {
        Result<?> result = clusterBrokerAutoUpdateService.save(cid, action);
        return Result.getWebResult(result);
    }

    /**
     * 就绪broker自动更新
     */
    @ResponseBody
    @PostMapping(value = "/autoUpdate/ready")
    public Result<?> readyAutoUpdate(@RequestParam(name = "id") int id, Map<String, Object> map) {
        return Result.getWebResult(updateAutoUpdate(id, BrokerAutoUpdate.Status.READY));
    }

    /**
     * 暂停broker自动更新
     */
    @ResponseBody
    @PostMapping(value = "/autoUpdate/pause")
    public Result<?> pauseAutoUpdate(@RequestParam(name = "id") int id, Map<String, Object> map) {
        return Result.getWebResult(updateAutoUpdate(id, BrokerAutoUpdate.Status.PAUSE));
    }

    /**
     * 运行broker自动更新
     */
    @ResponseBody
    @PostMapping(value = "/autoUpdate/start")
    public Result<?> startAutoUpdate(@RequestParam(name = "id") int id, Map<String, Object> map) {
        return Result.getWebResult(updateAutoUpdate(id, BrokerAutoUpdate.Status.RUNNING));
    }

    /**
     * 人为结束broker自动更新
     */
    @ResponseBody
    @PostMapping(value = "/autoUpdate/finish")
    public Result<?> finishAutoUpdate(@RequestParam(name = "id") int id, Map<String, Object> map) {
        return Result.getWebResult(updateAutoUpdate(id, BrokerAutoUpdate.Status.FINISHED));
    }

    public Result<?> updateAutoUpdate(int id, BrokerAutoUpdate.Status status) {
        BrokerAutoUpdate brokerAutoUpdate = new BrokerAutoUpdate();
        brokerAutoUpdate.setId(id);
        brokerAutoUpdate.setStatus(status.getValue());
        return brokerAutoUpdateService.update(brokerAutoUpdate);
    }

    /**
     * broker自动更新步骤列表
     */
    @GetMapping(value = "/autoUpdate/step/list")
    public String autoUpdateStepList(@RequestParam(name = "id") int id, Map<String, Object> map) {
        Result<BrokerAutoUpdate> brokerAutoUpdateResult = brokerAutoUpdateService.selectById(id);
        if (brokerAutoUpdateResult.isOK()) {
            setResult(map, "brokerAutoUpdate", brokerAutoUpdateResult.getResult());
        }
        setResult(map, brokerAutoUpdateStepService.selectByBrokerAutoUpdateId(id));
        return adminViewModule() + "/autoUpdate/stepList";
    }

    /**
     * 人为结束broker自动更新步骤
     */
    @ResponseBody
    @PostMapping(value = "/autoUpdateStep/finish")
    public Result<?> finishAutoUpdateStep(@RequestParam(name = "id") int id, Map<String, Object> map) {
        return Result.getWebResult(updateAutoUpdateStep(id, BrokerAutoUpdateStep.Status.FINISHED, new Date()));
    }

    /**
     * 重置broker自动更新步骤
     */
    @ResponseBody
    @PostMapping(value = "/autoUpdateStep/reset")
    public Result<?> resetAutoUpdateStep(@RequestParam(name = "id") int id, Map<String, Object> map) {
        return Result.getWebResult(updateAutoUpdateStep(id, BrokerAutoUpdateStep.Status.INIT, null));
    }

    public Result<?> updateAutoUpdateStep(int id, BrokerAutoUpdateStep.Status status, Date endTime) {
        BrokerAutoUpdateStep brokerAutoUpdateStep = new BrokerAutoUpdateStep();
        brokerAutoUpdateStep.setId(id);
        brokerAutoUpdateStep.setStatus(status.getValue());
        if (endTime != null) {
            brokerAutoUpdateStep.setEndTime(endTime);
        }
        brokerAutoUpdateStep.setInfo("");
        return brokerAutoUpdateStepService.update(brokerAutoUpdateStep);
    }

    @Override
    public String viewModule() {
        return "broker";
    }
}
