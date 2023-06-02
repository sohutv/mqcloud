package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.MessageQueryCondition;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.MessageDelayLevel;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.ClusterParam;
import com.sohu.tv.mq.cloud.web.vo.BrokerStatVO;
import com.sohu.tv.mq.cloud.web.vo.ClusterInfoVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.KVTable;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.running.RunningStats;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.*;

/**
 * 集群
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月5日
 */
@Controller
@RequestMapping("/admin/cluster")
public class AdminClusterController extends AdminViewController {

    @Autowired
    private MQAdminTemplate mqAdminTemplate;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private BrokerService brokerService;

    @RequestMapping("/list")
    public String list(@RequestParam(name = "cid", required = false) Integer cid, Map<String, Object> map) {
        setView(map, "list");
        Cluster mqCluster = getMQCluster(cid);
        if (mqCluster == null) {
            return view();
        }
        Result<List<Broker>> brokerListResult = null;
        // 从数据库查询当前集群的broker列表
        brokerListResult = brokerService.query(mqCluster.getId());
        // 数据库不存在broker列表时，从nameserver拉取
        if (brokerListResult.isNotOK()) {
            brokerListResult = getBrokerListFromNameServer(mqCluster);
        }
        Map<String, Map<String, BrokerStatVO>> brokerGroup = null;
        if (brokerListResult.isOK()) {
            List<Broker> brokerList = brokerListResult.getResult();
            brokerGroup = fetchBrokerRuntimeStats(brokerList, mqCluster);
        }
        // 生成vo
        ClusterInfoVO clusterInfoVO = new ClusterInfoVO();
        // 发生异常
        if (brokerGroup != null) {
            clusterInfoVO.setHasNameServer(true);
        }
        if (brokerGroup != null && brokerGroup.size() > 0) {
            clusterInfoVO.setBrokerGroup(brokerGroup);
        }
        clusterInfoVO.setMqCluster(clusterService.getAllMQCluster());
        clusterInfoVO.setSelectedMQCluster(mqCluster);
        setResult(map, clusterInfoVO);
        setResult(map, "username", mqCloudConfigHelper.getServerUser());
        // 查询延时消息用
        MessageQueryCondition messageQueryCondition = new MessageQueryCondition();
        messageQueryCondition.setCid(mqCluster.getId());
        messageQueryCondition.setTopic("SCHEDULE_TOPIC_XXXX");
        setResult(map, "messageQueryCondition", messageQueryCondition);
        setResult(map, "mqcloudDomain", mqCloudConfigHelper.getDomain());
        return view();
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
        Cluster mqCluster = getMQCluster(cid);
        logger.warn("nowrite {}:{}, user:{}", mqCluster, addr, ui);
        Result<Broker> brokerResult = brokerService.queryBroker(cid, addr);
        if (brokerResult.isNotOK()) {
            return Result.getWebResult(brokerResult);
        }
        Broker broker = brokerResult.getResult();
        Integer rst = mqAdminTemplate.execute(new DefaultCallback<Integer>() {
            public Integer callback(MQAdminExt mqAdmin) throws Exception {
                List<String> namesrvList = mqAdmin.getNameServerAddressList();
                if (namesrvList == null) {
                    return null;
                }
                int totalWipeTopicCount = 0;
                for (String namesrvAddr : namesrvList) {
                    try {
                        int wipeTopicCount = mqAdmin.wipeWritePermOfBroker(namesrvAddr, broker.getBrokerName());
                        totalWipeTopicCount += wipeTopicCount;
                    } catch (Exception e) {
                        logger.error("nowrite namesrvAddr:{}, broker:{} err", namesrvAddr, broker.getBrokerName(), e);
                        throw e;
                    }
                }
                brokerService.updateWritable(cid, broker.getAddr(), false);
                return totalWipeTopicCount;
            }

            public Cluster mqCluster() {
                return mqCluster;
            }
        });
        return Result.getResult(rst);
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
        Cluster mqCluster = getMQCluster(cid);
        logger.warn("resumeWrite {}:{}, user:{}", mqCluster, addr, ui);
        Result<Broker> brokerResult = brokerService.queryBroker(cid, addr);
        if (brokerResult.isNotOK()) {
            return Result.getWebResult(brokerResult);
        }
        Broker broker = brokerResult.getResult();
        return mqAdminTemplate.execute(new DefaultCallback<Result<?>>() {
            public Result<?> callback(MQAdminExt mqAdmin) throws Exception {
                List<String> namesrvList = mqAdmin.getNameServerAddressList();
                if (namesrvList == null) {
                    return Result.getResult(null);
                }
                SohuMQAdmin sohuMQAdmin = (SohuMQAdmin) mqAdmin;
                for (String namesrvAddr : namesrvList) {
                    try {
                        sohuMQAdmin.unregisterBroker(namesrvAddr, mqCluster.getName(), broker.getAddr(), broker.getBrokerName(), broker.getBrokerID());
                    } catch (Exception e) {
                        logger.error("unregisterBroker namesrvAddr:{}, broker:{} err", namesrvAddr, broker, e);
                        return Result.getWebErrorResult(e);
                    }
                }
                brokerService.updateWritable(cid, broker.getAddr(), true);
                return Result.getOKResult();
            }
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
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
    public String initTopic(UserInfo userInfo, @RequestParam("cid") Integer cid, Map<String, Object> map)
            throws Exception {
        Cluster cluster = clusterService.getMQClusterById(cid);
        Result<?> result = topicService.initTopic(cluster);
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
    public String initConsumer(UserInfo userInfo, @RequestParam("cid") Integer cid, Map<String, Object> map)
            throws Exception {
        Cluster cluster = clusterService.getMQClusterById(cid);
        Result<List<Topic>> topicListResult = topicService.queryTopicList(cluster);
        if (topicListResult.isEmpty()) {
            setResult(map, Result.getWebResult(topicListResult));
            return adminViewModule() + "/initConsumer";
        }
        Map<String, List<Result>> resultMap = consumerService.initConsumer(cluster, topicListResult.getResult());
        setResult(map, Result.getResult(resultMap));
        return adminViewModule() + "/initConsumer";
    }

    /**
     * 新增cluster
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> addCluster(UserInfo userInfo, @Valid ClusterParam clusterParam, Map<String, Object> map)
            throws Exception {
        Cluster cluster = new Cluster();
        BeanUtils.copyProperties(clusterParam, cluster);
        Result<?> result = clusterService.save(cluster);
        return result;
    }

    private String removeFromMap(HashMap<String, String> map, String key) {
        return map.remove(key);
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

    private String formatTraffic(String value) {
        String[] array = value.split(" ");
        if (array != null && array.length > 0) {
            return String.format("%.2f", NumberUtils.toDouble(array[0]));
        }
        return "-";
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
                HashMap<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
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
    private Map<String, Map<String, BrokerStatVO>> fetchBrokerRuntimeStats(List<Broker> brokerList,
            Cluster mqCluster) {
        Map<String, Map<String, BrokerStatVO>> brokerGroup = mqAdminTemplate
                .execute(new MQAdminCallback<Map<String, Map<String, BrokerStatVO>>>() {
                    public Map<String, Map<String, BrokerStatVO>> callback(MQAdminExt mqAdmin) throws Exception {
                        Map<String, Map<String, BrokerStatVO>> brokerGroup = new TreeMap<String, Map<String, BrokerStatVO>>();
                        for (Broker broker : brokerList) {
                            Map<String, BrokerStatVO> brokerStatMap = brokerGroup.get(broker.getBrokerName());
                            if (brokerStatMap == null) {
                                brokerStatMap = new TreeMap<String, BrokerStatVO>();
                                brokerGroup.put(broker.getBrokerName(), brokerStatMap);
                            }
                            // 公共逻辑 拼接BrokerStatVO
                            BrokerStatVO brokerStatVO = new BrokerStatVO();
                            brokerStatVO.setBrokerAddr(broker.getAddr());
                            brokerStatVO.setBaseDir(broker.getBaseDir());
                            brokerStatMap.put(String.valueOf(broker.getBrokerID()), brokerStatVO);
                            // 监控结果
                            brokerStatVO.setCheckStatus(broker.getCheckStatus());
                            brokerStatVO.setCheckTime(broker.getCheckTimeFormat());
                            brokerStatVO.setWritable(broker.isWritable());
                            // 当有broker down时，数据库中的broker 地址已过时，增加异常处理
                            try {
                                // 抓取统计指标
                                KVTable kvTable = mqAdmin.fetchBrokerRuntimeStats(broker.getAddr());
                                // 处理broker stats 数据
                                handleBrokerStat(broker, kvTable, brokerStatVO);
                            } catch (Exception e) {
                                logger.error("cluster:{} broker:{} err", mqCluster(), broker.getAddr(), e);
                            }
                        }
                        return brokerGroup;
                    }

                    public Cluster mqCluster() {
                        return mqCluster;
                    }

                    public Map<String, Map<String, BrokerStatVO>> exception(Exception e) throws Exception {
                        logger.error("cluster:{} err", mqCluster(), e);
                        return null;
                    }
                });
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
            if (offsetString == null || broker.getBrokerID() != 0) {
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
    }

    @Override
    public String viewModule() {
        return "cluster";
    }
}
