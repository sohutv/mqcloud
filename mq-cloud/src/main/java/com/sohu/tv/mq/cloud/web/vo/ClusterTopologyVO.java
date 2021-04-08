package com.sohu.tv.mq.cloud.web.vo;

import java.util.List;

import com.sohu.tv.mq.cloud.bo.BrokerTraffic;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.ServerInfo;

/**
 * 集群拓扑VO
 * 
 * @author yongfeigao
 * @date 2020年4月16日
 */
public class ClusterTopologyVO {
    private Cluster cluster;
    private List<ServerInfo> nameServerList;
    private List<BrokerGroup> brokerGroupList;
    private BrokerTraffic clusterTraffic;

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public List<ServerInfo> getNameServerList() {
        return nameServerList;
    }

    public void setNameServerList(List<ServerInfo> nameServerList) {
        this.nameServerList = nameServerList;
    }

    public List<BrokerGroup> getBrokerGroupList() {
        return brokerGroupList;
    }

    public void setBrokerGroupList(List<BrokerGroup> brokerGroupList) {
        this.brokerGroupList = brokerGroupList;
    }

    public BrokerTraffic getClusterTraffic() {
        return clusterTraffic;
    }

    public void setClusterTraffic(BrokerTraffic clusterTraffic) {
        this.clusterTraffic = clusterTraffic;
    }


    public static class BrokerGroup {
        private BrokerStat master;
        private BrokerStat slave;
        private String brokerName;

        public String getBrokerName() {
            return brokerName;
        }

        public void setBrokerName(String brokerName) {
            this.brokerName = brokerName;
        }

        public BrokerStat getMaster() {
            return master;
        }

        public void setMaster(BrokerStat master) {
            this.master = master;
        }

        public BrokerStat getSlave() {
            return slave;
        }

        public void setSlave(BrokerStat slave) {
            this.slave = slave;
        }
    }

    public static class BrokerStat {
        // broker id
        private int brokerID;
        private String brokerName;
        private ServerInfo serverInfo;
        private BrokerTraffic brokerTraffic;
        private String addr;

        public int getBrokerID() {
            return brokerID;
        }

        public void setBrokerID(int brokerID) {
            this.brokerID = brokerID;
        }

        public String getBrokerName() {
            return brokerName;
        }

        public void setBrokerName(String brokerName) {
            this.brokerName = brokerName;
        }

        public ServerInfo getServerInfo() {
            return serverInfo;
        }

        public void setServerInfo(ServerInfo serverInfo) {
            this.serverInfo = serverInfo;
        }

        public BrokerTraffic getBrokerTraffic() {
            return brokerTraffic;
        }

        public void setBrokerTraffic(BrokerTraffic brokerTraffic) {
            this.brokerTraffic = brokerTraffic;
        }

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }
    }
}
