package com.sohu.tv.mq.dto;
/**
 * 集群信息结果
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月3日
 */
public class ClusterInfoDTOResult extends AbstractResult {
    private ClusterInfoDTO result;

    public ClusterInfoDTO getResult() {
        return result;
    }

    public void setResult(ClusterInfoDTO result) {
        this.result = result;
    }
}
