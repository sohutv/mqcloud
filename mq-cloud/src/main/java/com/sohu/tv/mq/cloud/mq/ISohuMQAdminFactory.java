package com.sohu.tv.mq.cloud.mq;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.common.mq.SohuMQAdmin;

/**
 * sohu mq admin 工厂
 *
 * @Auther: yongfeigao
 * @Date: 2023/6/20
 */
public interface ISohuMQAdminFactory {

    /**
     * 获取启动好的实例
     *
     * @param key
     * @return
     * @throws Exception
     */
    SohuMQAdmin getInstance(Cluster key) throws Exception;
}
