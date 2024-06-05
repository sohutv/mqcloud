package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.task.server.data.Disk.DiskUsage;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务器角色-broker，nameserver等
 *
 * @author: yongfeigao
 * @date: 2022/3/29 14:44
 */
public class ServerRoleVO {
    private List<RoleVO> roleVOList = new ArrayList<>();

    private List<DiskUsage> diskUsageList = new ArrayList<>();

    public void addBroker(Broker broker, Cluster cluster) {
        RoleVO roleVO = new RoleVO();
        roleVO.setPort(NumberUtils.toInt(broker.getAddr().split(":")[1]));
        roleVO.setBaseDir(broker.getBaseDir());
        roleVO.setClusterName(cluster.getName());
        roleVO.setClusterId(cluster.getId());
        roleVO.setBroker(true);
        roleVO.setDeployment(broker.getBrokerName() + ":" + broker.getBrokerID());
        roleVOList.add(roleVO);
    }

    public void addNameServer(NameServer nameServer, Cluster cluster) {
        RoleVO roleVO = new RoleVO();
        roleVO.setPort(NumberUtils.toInt(nameServer.getAddr().split(":")[1]));
        roleVO.setBaseDir(nameServer.getBaseDir());
        roleVO.setClusterName(cluster.getName());
        roleVO.setClusterId(cluster.getId());
        roleVO.setDeployment("ns");
        roleVOList.add(roleVO);
    }

    public void addProxy(Proxy proxy, Cluster cluster) {
        RoleVO roleVO = new RoleVO();
        roleVO.setPort(NumberUtils.toInt(proxy.getAddr().split(":")[1]));
        roleVO.setBaseDir(proxy.getBaseDir());
        roleVO.setClusterName(cluster.getName());
        roleVO.setClusterId(cluster.getId());
        roleVO.setDeployment("proxy");
        roleVOList.add(roleVO);
    }

    public void addController(Controller controller, Cluster cluster) {
        RoleVO roleVO = new RoleVO();
        roleVO.setPort(NumberUtils.toInt(controller.getAddr().split(":")[1]));
        roleVO.setBaseDir(controller.getBaseDir());
        roleVO.setClusterName(cluster.getName());
        roleVO.setClusterId(cluster.getId());
        roleVO.setDeployment("controller");
        roleVOList.add(roleVO);
    }

    public void initDiskUsage(ServerInfoExt serverInfoExt) {
        for (RoleVO roleVO : roleVOList) {
            DiskUsage diskUsage = serverInfoExt.getDiskUsage(roleVO.getBaseDir());
            addToDiskUsageList(diskUsage);
        }
        if (diskUsageList.isEmpty()) {
            List<DiskUsage> diskUsages = serverInfoExt.getDiskUsage();
            if (diskUsages != null) {
                for (DiskUsage diskUsage : diskUsages) {
                    addToDiskUsageList(diskUsage);
                }
            }
        }
    }

    private void addToDiskUsageList(DiskUsage diskUsage) {
        if (diskUsage == null) {
            return;
        }
        for (DiskUsage du : diskUsageList) {
            if (diskUsage.getName().equals(du.getName())) {
                return;
            }
        }
        diskUsageList.add(diskUsage);
    }

    public RoleVO getFirstRoleVO() {
        return roleVOList.get(0);
    }

    public List<DiskUsage> getDiskUsageList() {
        return diskUsageList;
    }

    public void setDiskUsageList(List<DiskUsage> diskUsageList) {
        this.diskUsageList = diskUsageList;
    }

    /**
     * 角色
     */
    public class RoleVO {
        // 端口
        private int port;
        // 部署，类似broker-b:0或ns
        private String deployment;
        // 安装路径
        private String baseDir;
        // 集群名
        private String clusterName;
        // 集群id
        private int clusterId;
        // 是否是broker
        private boolean broker;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isBroker() {
            return broker;
        }

        public void setBroker(boolean broker) {
            this.broker = broker;
        }

        public String getBaseDir() {
            return baseDir;
        }

        public void setBaseDir(String baseDir) {
            this.baseDir = baseDir;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public String getDeployment() {
            return deployment;
        }

        public void setDeployment(String deployment) {
            this.deployment = deployment;
        }

        public int getClusterId() {
            return clusterId;
        }

        public void setClusterId(int clusterId) {
            this.clusterId = clusterId;
        }
    }

    public List<RoleVO> getRoleVOList() {
        return roleVOList;
    }
}
