package com.sohu.tv.mq.cloud.web.vo;

import com.sohu.tv.mq.cloud.bo.ClusterCapacity;
import com.sohu.tv.mq.cloud.bo.ClusterCapacity.BrokerCapacity;
import com.sohu.tv.mq.cloud.util.WebUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 集群容量
 *
 * @Description:
 * @date 2024年5月29日
 */
public class ClusterCapacityVO {
    private List<ClusterCapacity> clusterCapacityList;
    private String clusterCapacityLink;

    public List<ClusterCapacity> getClusterCapacityList() {
        return clusterCapacityList;
    }

    public void setClusterCapacityList(List<ClusterCapacity> clusterCapacityList) {
        this.clusterCapacityList = clusterCapacityList;
    }

    public void addClusterDataCapacity(ClusterCapacity clusterCapacity) {
        if (clusterCapacityList == null) {
            clusterCapacityList = new ArrayList<>();
        }
        clusterCapacityList.add(clusterCapacity);
    }

    public void sortClusterDataCapacity() {
        clusterCapacityList.forEach(ClusterCapacity::sortBrokerCapacityList);
    }

    public String getClusterCapacityLink() {
        return clusterCapacityLink;
    }

    public void setClusterCapacityLink(String clusterCapacityLink) {
        this.clusterCapacityLink = clusterCapacityLink;
    }

    public long getSize1d() {
        if (clusterCapacityList == null || clusterCapacityList.isEmpty()) {
            return 0;
        }
        long size1d = 0;
        for (ClusterCapacity clusterCapacity : clusterCapacityList) {
            size1d += clusterCapacity.getSize1d();
        }
        return size1d;
    }

    public String getSize1dFormat() {
        return WebUtil.sizeFormat(getSize1d());
    }
}
