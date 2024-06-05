package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.cloud.task.server.data.Disk.DiskUsage;
import com.sohu.tv.mq.cloud.util.WebUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 集群容量
 *
 * @author yongfeigao
 * @date 2024年5月24日
 */
public class ClusterCapacity {
    private Cluster cluster;
    private List<BrokerCapacity> brokerCapacityList;

    private List<Topic> topicList;

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public List<BrokerCapacity> getBrokerCapacityList() {
        return brokerCapacityList;
    }

    public void setBrokerCapacityList(List<BrokerCapacity> brokerCapacityList) {
        this.brokerCapacityList = brokerCapacityList;
    }

    public List<Topic> getTopicList() {
        return topicList;
    }

    public void setTopicList(List<Topic> topicList) {
        this.topicList = topicList;
    }

    public void addBrokerCapacity(BrokerCapacity brokerCapacity) {
        if (brokerCapacityList == null) {
            brokerCapacityList = new ArrayList<>();
        }
        brokerCapacityList.add(brokerCapacity);
    }

    public long getSize1d() {
        if (brokerCapacityList == null || brokerCapacityList.isEmpty()) {
            return 0;
        }
        long size1d = 0;
        for (BrokerCapacity brokerCapacity : brokerCapacityList) {
            size1d += brokerCapacity.getBroker().getSize1d();
        }
        return size1d;
    }

    public String getSize1dFormat() {
        return WebUtil.sizeFormat(getSize1d());
    }

    public void sortBrokerCapacityList() {
        if (brokerCapacityList == null || brokerCapacityList.isEmpty()) {
            return;
        }
        brokerCapacityList.sort((o1, o2) -> {
            // 首先按照硬盘剩余天数排序，剩余天数小的排在前面
            double leftDays = o1.getEstimateLeftDays() - o2.getEstimateLeftDays();
            if (leftDays > 0) {
                return 1;
            } else if (leftDays < 0) {
                return -1;
            }
            // 其次按照昨日写入量排序，写入量大的排在前面
            if (o1.getBroker().getSize1d() == o2.getBroker().getSize1d()) {
                int nameCompare = o1.getBroker().getBrokerName().compareTo(o2.getBroker().getBrokerName());
                if (nameCompare == 0) {
                    return o1.getBroker().getBrokerID() - o2.getBroker().getBrokerID();
                }
                return nameCompare;
            }
            return o1.getBroker().getSize1d() > o2.getBroker().getSize1d() ? -1 : 1;
        });
    }

    public static class BrokerCapacity {
        private Broker broker;
        private ServerInfoExt serverInfoExt;
        private DiskUsage diskUsage;

        public BrokerCapacity(Broker broker, ServerInfoExt serverInfoExt) {
            this.broker = broker;
            this.serverInfoExt = serverInfoExt;
            this.diskUsage = serverInfoExt.getDiskUsage(broker.getBaseDir());
        }

        public Broker getBroker() {
            return broker;
        }

        public void setBroker(Broker broker) {
            this.broker = broker;
        }

        public ServerInfoExt getServerInfoExt() {
            return serverInfoExt;
        }

        public void setServerInfoExt(ServerInfoExt serverInfoExt) {
            this.serverInfoExt = serverInfoExt;
        }

        public DiskUsage getDiskUsage() {
            return diskUsage;
        }

        public String getServerTime() {
            return serverInfoExt.getCdate() + " " + serverInfoExt.getCtime();
        }

        public double getEstimateLeftDays() {
            double avgSizeM = broker.getSize3d() / 3 / 1024 / 1024;
            if (avgSizeM == 0) {
                return 1000d;
            }
            if (diskUsage == null) {
                return 1000d;
            }
            double leftDays = (diskUsage.getSize() - diskUsage.getUsed()) / avgSizeM;
            // 如果剩余天数大于10天,则返回整数
            if (leftDays >= 10) {
                return (long) leftDays;
            }
            // 保留两位小数
            leftDays = Math.round(leftDays * 100) / 100D;
            return leftDays;
        }
    }
}
