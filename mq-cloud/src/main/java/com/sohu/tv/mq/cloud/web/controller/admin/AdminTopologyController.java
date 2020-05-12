package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sohu.tv.mq.cloud.bo.Broker;
import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.NameServer;
import com.sohu.tv.mq.cloud.bo.ServerInfo;
import com.sohu.tv.mq.cloud.service.BrokerService;
import com.sohu.tv.mq.cloud.service.BrokerTrafficService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.NameServerService;
import com.sohu.tv.mq.cloud.service.ServerDataService;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.vo.ClusterTopologyVO;
import com.sohu.tv.mq.cloud.web.vo.ClusterTopologyVO.BrokerGroup;
import com.sohu.tv.mq.cloud.web.vo.ClusterTopologyVO.BrokerStat;

/**
 * 集群拓扑
 * 
 * @author yongfeigao
 * @date 2020年4月16日
 */
@Controller
@RequestMapping("/admin/topology")
public class AdminTopologyController extends AdminViewController {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private NameServerService nameServerService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private BrokerTrafficService brokerTrafficService;

    @Autowired
    private ServerDataService serverDataService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    /**
     * 集群拓扑
     * 
     * @param cid
     * @param map
     * @return
     */
    @RequestMapping("/cluster")
    public String topology(Map<String, Object> map) {
        setView(map, "cluster");
        Cluster[] clusters = clusterService.getAllMQCluster();
        if (clusters == null) {
            return view();
        }
        // 获取所有namesrv
        Result<List<NameServer>> nsListResult = nameServerService.queryAll();
        if (nsListResult.isEmpty()) {
            return view();
        }
        // 获取所有broker
        Result<List<Broker>> brokerListResult = brokerService.queryAll();
        List<Broker> brokers = brokerListResult.getResult();
        // 获取broker流量
        List<BrokerTraffic> brokerTraffics = getBrokerTrafficList(brokers);
        // 进行组装
        setResult(map, buildClusterTopologyVO(clusters, nsListResult.getResult(), brokers, brokerTraffics));
        // 设置机房颜色
        setResult(map, "machineRoomColorMap", getMachineRoomColorMap());
        return view();
    }
    
    /**
     * 流量
     * 
     * @param cid
     * @param map
     * @return
     */
    @RequestMapping("/traffic")
    public String traffic(@RequestParam("ips") String ips, Map<String, Object> map) {
        // 获取日期
        Date now = new Date();
        String date = DateUtil.getFormat(DateUtil.YMD_DASH).format(now);
        // 获取前60分钟数据
        String time = DateUtil.getFormat(DateUtil.HHMM).format(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
        String[] ipArray = ips.split(",");
        if(ipArray.length == 1) {
            setResult(map, brokerTrafficService.queryTrafficStatistic(date, ips, time));
        } else {
            List<String> ipList = Arrays.asList(ips.split(","));
            setResult(map, brokerTrafficService.queryTrafficStatistic(date, ipList, time));
        }
        return adminViewModule() + "/traffic";
    }

    private Map<String, String> getMachineRoomColorMap() {
        Map<String, String> machineRoomColorMap = new LinkedHashMap<>();
        for (String machineRoom : mqCloudConfigHelper.getMachineRoom()) {
            machineRoomColorMap.put(machineRoom, mqCloudConfigHelper.getMachineRoomColor(machineRoom));
        }
        return machineRoomColorMap;
    }

    /**
     * 构建ClusterTopologyVO
     * 
     * @param clusters
     * @param nameServers
     * @param brokers
     * @param brokerTraffics
     * @return
     */
    private List<ClusterTopologyVO> buildClusterTopologyVO(Cluster[] clusters, List<NameServer> nameServers,
            List<Broker> brokers, List<BrokerTraffic> brokerTraffics) {
        List<ClusterTopologyVO> list = new ArrayList<>(clusters.length);
        // 获取所有服务器信息
        List<ServerInfo> serverInfoList = serverDataService.queryAllServerInfo();
        for (Cluster cluster : clusters) {
            ClusterTopologyVO clusterTopologyVO = new ClusterTopologyVO();
            clusterTopologyVO.setCluster(cluster);
            // 获取NameServer
            clusterTopologyVO.setNameServerList(filterNameServers(cluster.getId(), nameServers, serverInfoList));
            // 获取broker
            clusterTopologyVO
                    .setBrokerGroupList(filterBroker(cluster.getId(), brokers, brokerTraffics, serverInfoList));
            // 计算集群流量
            caculateClusterTraffic(clusterTopologyVO);
            list.add(clusterTopologyVO);
        }
        return list;
    }

    /**
     * 计算集群流量
     * 
     * @param clusterTopologyVO
     */
    private void caculateClusterTraffic(ClusterTopologyVO clusterTopologyVO) {
        List<BrokerGroup> brokerGroupList = clusterTopologyVO.getBrokerGroupList();
        if (brokerGroupList == null) {
            return;
        }
        BrokerTraffic clusterTraffic = new BrokerTraffic();
        for (BrokerGroup brokerGroup : brokerGroupList) {
            BrokerTraffic brokerTraffic = brokerGroup.getMaster().getBrokerTraffic();
            if (brokerTraffic == null) {
                continue;
            }
            clusterTraffic.add(brokerTraffic);
        }
        clusterTopologyVO.setClusterTraffic(clusterTraffic);
    }

    /**
     * 过滤并获取NameServer
     * 
     * @param cid
     * @param nameServers
     * @return
     */
    private List<ServerInfo> filterNameServers(int cid, List<NameServer> nameServers, List<ServerInfo> serverInfoList) {
        List<ServerInfo> nsList = new ArrayList<>();
        Iterator<NameServer> it = nameServers.iterator();
        while (it.hasNext()) {
            NameServer ns = it.next();
            if (ns.getCid() == cid) {
                ServerInfo serverInfo = findServerInfo(serverInfoList, ns.getIp());
                if (serverInfo != null) {
                    nsList.add(serverInfo);
                }
                it.remove();
            }
        }
        if (nsList.size() == 0) {
            return null;
        }
        Collections.sort(nsList, (a, b) -> {
            return a.getIp().compareTo(b.getIp());
        });
        return nsList;
    }

    private ServerInfo findServerInfo(List<ServerInfo> serverInfoList, String ip) {
        for (ServerInfo serverInfo : serverInfoList) {
            if (ip.equals(serverInfo.getIp())) {
                serverInfo.setRoomColor(mqCloudConfigHelper.getMachineRoomColor(serverInfo.getRoom()));
                return serverInfo;
            }
        }
        return null;
    }

    /**
     * 过滤并获取broker
     * 
     * @param cid
     * @param brokers
     * @param brokerTraffics
     * @return
     */
    private List<BrokerGroup> filterBroker(int cid, List<Broker> brokers,
            List<BrokerTraffic> brokerTraffics, List<ServerInfo> serverInfoList) {
        if (brokers == null || brokers.size() == 0) {
            return null;
        }
        List<BrokerGroup> brokerGroupList = new ArrayList<>();
        Iterator<Broker> brokerIterator = brokers.iterator();
        while (brokerIterator.hasNext()) {
            Broker broker = brokerIterator.next();
            if (cid == broker.getCid()) {
                brokerIterator.remove();
                BrokerStat brokerStat = new BrokerStat();
                brokerStat.setBrokerID(broker.getBrokerID());
                brokerStat.setBrokerName(broker.getBrokerName());
                // 获取服务器信息
                ServerInfo serverInfo = findServerInfo(serverInfoList, broker.getIp());
                if (serverInfo != null) {
                    brokerStat.setServerInfo(serverInfo);
                }
                // 设置流量
                if (brokerTraffics != null) {
                    Iterator<BrokerTraffic> brokerTrafficIterator = brokerTraffics.iterator();
                    while (brokerTrafficIterator.hasNext()) {
                        BrokerTraffic brokerTraffic = brokerTrafficIterator.next();
                        if (cid == brokerTraffic.getClusterId() && broker.getAddr().startsWith(brokerTraffic.getIp())) {
                            brokerStat.setBrokerTraffic(brokerTraffic);
                        }
                    }
                }
                // 添加到broker group
                addToBrokerGroupList(brokerStat, brokerGroupList);
            }
        }
        if (brokerGroupList.size() == 0) {
            return null;
        }
        Collections.sort(brokerGroupList, (a, b) -> {
            return a.getBrokerName().compareTo(b.getBrokerName());
        });
        return brokerGroupList;
    }

    /**
     * 添加到列表
     * 
     * @param brokerStat
     * @param brokerGroupList
     */
    private void addToBrokerGroupList(BrokerStat brokerStat, List<BrokerGroup> brokerGroupList) {
        for (BrokerGroup brokerGroup : brokerGroupList) {
            if (brokerStat.getBrokerName().equals(brokerGroup.getBrokerName())) {
                if (brokerStat.getBrokerID() == 0) {
                    brokerGroup.setMaster(brokerStat);
                } else {
                    brokerGroup.setSlave(brokerStat);
                }
                return;
            }
        }
        BrokerGroup brokerGroup = new BrokerGroup();
        brokerGroup.setBrokerName(brokerStat.getBrokerName());
        if (brokerStat.getBrokerID() == 0) {
            brokerGroup.setMaster(brokerStat);
        } else {
            brokerGroup.setSlave(brokerStat);
        }
        brokerGroupList.add(brokerGroup);
    }

    /**
     * 获取broker流量
     * 
     * @param brokers
     * @return
     */
    private List<BrokerTraffic> getBrokerTrafficList(List<Broker> brokers) {
        if (brokers == null || brokers.size() == 0) {
            return null;
        }
        // 获取ip
        List<String> ips = new ArrayList<>(brokers.size());
        for (Broker broker : brokers) {
            if (broker.getBrokerID() != 0) {
                continue;
            }
            String addr = broker.getAddr();
            String ip = addr.split(":")[0];
            ips.add(ip);
        }
        // 获取日期
        Date now = new Date();
        String date = DateUtil.getFormat(DateUtil.YMD_DASH).format(now);
        // 获取前5分钟数据
        int timeSize = 5;
        List<String> times = new ArrayList<>(timeSize);
        for (int i = 1; i <= timeSize; ++i) {
            now.setTime(now.getTime() - 60 * 1000);
            String time = DateUtil.getFormat(DateUtil.HHMM).format(now);
            times.add(time);
        }
        Result<List<BrokerTraffic>> brokerTrafficListResult = brokerTrafficService.queryTraffic(date, times, ips);
        if (brokerTrafficListResult.isEmpty()) {
            return null;
        }
        return brokerTrafficListResult.getResult();
    }

    @Override
    public String viewModule() {
        return "topology";
    }
}
