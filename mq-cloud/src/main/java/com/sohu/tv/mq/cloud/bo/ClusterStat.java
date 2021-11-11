package com.sohu.tv.mq.cloud.bo;

import java.util.List;

/**
 * 集群状态
 * 
 * @author yongfeigao
 * @date 2021年9月18日
 */
public class ClusterStat {
    // 集群名
    private String clusterName;
    // 集群链接
    private String clusterLink;
    // 状态
    private List<String> stats;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterLink() {
        return clusterLink;
    }

    public void setClusterLink(String clusterLink) {
        this.clusterLink = clusterLink;
    }

    public List<String> getStats() {
        return stats;
    }

    public void setStats(List<String> stats) {
        this.stats = stats;
    }
}
