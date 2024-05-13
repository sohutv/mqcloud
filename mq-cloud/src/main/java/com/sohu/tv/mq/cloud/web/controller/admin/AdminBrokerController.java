package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.common.model.BrokerRateLimitData;
import com.sohu.tv.mq.cloud.common.model.UpdateSendMsgRateLimitRequestHeader;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.BrokerConfigParam;
import com.sohu.tv.mq.cloud.web.controller.param.BrokerConfigUpdateParam;
import com.sohu.tv.mq.cloud.web.controller.param.UpdateSendMsgRateLimitParam;
import com.sohu.tv.mq.cloud.web.vo.BrokerConfigGroupVO;
import com.sohu.tv.mq.cloud.web.vo.BrokerConfigVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.common.constant.PermName;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.QueueData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @Override
    public String viewModule() {
        return "broker";
    }
}
