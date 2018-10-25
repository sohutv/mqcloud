package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.validation.Valid;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.KVTable;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.ClusterParam;
import com.sohu.tv.mq.cloud.web.vo.BrokerStatVO;
import com.sohu.tv.mq.cloud.web.vo.ClusterInfoVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

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
    
    @RequestMapping("/list")
    public String list(@RequestParam(name="cid", required=false) Integer cid, Map<String, Object> map) {
        setView(map, "list");
        Cluster mqCluster = getMQCluster(cid);
        if(mqCluster == null) {
            return view();
        }
        Map<String, Map<String, BrokerStatVO>> brokerGroup = mqAdminTemplate.execute(new MQAdminCallback<Map<String, Map<String, BrokerStatVO>>>() {
            public Map<String, Map<String, BrokerStatVO>> callback(MQAdminExt mqAdmin) throws Exception {
                Map<String, Map<String, BrokerStatVO>> brokerGroup = new TreeMap<String, Map<String, BrokerStatVO>>();
                // 获取集群信息
                ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
                // 获得broker地址map
                HashMap<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();
                for(String brokerName : brokerAddrTable.keySet()) {
                    BrokerData brokerData = clusterInfo.getBrokerAddrTable().get(brokerName);
                    if (brokerData == null) {
                        continue;
                    }
                    
                    Map<String, BrokerStatVO> brokerStatMap = brokerGroup.get(brokerName);
                    if(brokerStatMap == null) {
                        brokerStatMap = new TreeMap<String, BrokerStatVO>();
                        brokerGroup.put(brokerName, brokerStatMap);
                    }
                    
                    // 获取broker运行时信息
                    HashMap<Long, String> brokerAddrs = brokerData.getBrokerAddrs();
                    for(Long brokerId : brokerAddrs.keySet()) {
                        String brokerAddr = brokerAddrs.get(brokerId);
                        // 抓取统计指标
                        KVTable kvTable = mqAdmin.fetchBrokerRuntimeStats(brokerAddr);
                        HashMap<String, String> stats = kvTable.getTable();
                        
                        BrokerStatVO brokerStatVO = new BrokerStatVO();
                        // 启动时间
                        String bootTime = getFromMap(stats, "bootTimestamp");
                        String boot = DateUtil.getFormat(DateUtil.YMD_BLANK_HMS_COLON).format(new Date(NumberUtils.toLong(bootTime)));
                        brokerStatVO.setBootTime(boot);
                        // 版本
                        brokerStatVO.setVersion(getFromMap(stats, "brokerVersionDesc"));
                        kvTable.getTable().remove("brokerVersion");
                        // 地址
                        brokerStatVO.setBrokerAddr(brokerAddr);
                        // 流量
                        brokerStatVO.setInTps(formatTraffic(getFromMap(stats, "putTps")));
                        brokerStatVO.setOutTps(formatTraffic(getFromMap(stats, "getTransferedTps")));
                        // 其余指标
                        brokerStatVO.setInfo(new TreeMap<String, String>(stats));
                        brokerStatMap.put(brokerId.toString(), brokerStatVO);
                    }
                }
                return brokerGroup;
            }
            public Cluster mqCluster() {
                return mqCluster;
            }
            public Map<String, Map<String, BrokerStatVO>> exception(Exception e) throws Exception {
                logger.error("cluster:{} err", mqCluster(), e.getMessage());
                return null;
            }
        });
        // 生成vo
        ClusterInfoVO clusterInfoVO = new ClusterInfoVO();
        // 发生异常
        if(brokerGroup != null) {
            clusterInfoVO.setHasNameServer(true);
        }
        if(brokerGroup != null && brokerGroup.size() > 0) {
            clusterInfoVO.setBrokerGroup(brokerGroup);
        }
        clusterInfoVO.setMqCluster(clusterService.getAllMQCluster());
        clusterInfoVO.setSelectedMQCluster(mqCluster);
        setResult(map, clusterInfoVO);
        setResult(map, "username", mqCloudConfigHelper.getServerUser());
        return view();
    }
    
    /**
     * 禁止写入
     * @param cid
     * @param broker
     * @return
     */
    @ResponseBody
    @RequestMapping(value="/nowrite", method=RequestMethod.POST)
    public Result<?> nowrite(UserInfo ui, @RequestParam(name="cid") Integer cid,
            @RequestParam(name="broker") String broker) {
        Cluster mqCluster = getMQCluster(cid);
        logger.warn("nowrite {}:{}, user:{}", mqCluster, broker, ui);
        Integer rst = mqAdminTemplate.execute(new DefaultCallback<Integer>() {
            public Integer callback(MQAdminExt mqAdmin) throws Exception {
                List<String> namesrvList = mqAdmin.getNameServerAddressList();
                if(namesrvList == null) {
                    return null;
                }
                int totalWipeTopicCount = 0;
                for (String namesrvAddr : namesrvList) {
                    try {
                        int wipeTopicCount = mqAdmin.wipeWritePermOfBroker(namesrvAddr, broker);
                        totalWipeTopicCount += wipeTopicCount;
                    } catch (Exception e) {
                        logger.error("nowrite namesrvAddr:{}, broker:{} err", namesrvAddr, broker, e);
                        throw e;
                    }
                }
                return totalWipeTopicCount;
            }
            public Cluster mqCluster() {
                return mqCluster;
            }
        });
        return Result.getResult(rst);
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
        if(topicListResult.isEmpty()) {
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
    public Result<?> initTopic(UserInfo userInfo, @Valid ClusterParam clusterParam, Map<String, Object> map)
            throws Exception {
        Cluster cluster = new Cluster();
        BeanUtils.copyProperties(clusterParam, cluster);
        Result<?> result = clusterService.save(cluster);
        return result;
    }
    
    private String getFromMap(HashMap<String, String> map, String key) {
        return map.remove(key);
    }
    
    private Cluster getMQCluster(Integer cid) {
        Cluster mqCluster = null;
        if(cid != null) {
            mqCluster = clusterService.getMQClusterById(cid);
        }
        if(mqCluster == null && clusterService.getAllMQCluster() != null) {
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
    
    @Override
    public String viewModule() {
        return "cluster";
    }
}
