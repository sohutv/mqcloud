package com.sohu.tv.mq.cloud.mq;

import org.apache.rocketmq.tools.admin.MQAdminExt;

import com.sohu.tv.mq.cloud.bo.Cluster;
/**
 * 操作回调类
 * @Description: 用于操作MQ的模板类回调
 * @author yongfeigao
 * @date 2018年5月24日
 * @param <T> 返回值类型
 */
public interface MQAdminCallback<T> {
    
    /**
     * 具体操作回调
     * @param mqAdmin
     * @return
     * @throws Exception
     */
	T callback(MQAdminExt mqAdmin) throws Exception;
	
	/**
	 * 发生异常时回调
	 * @param e
	 * @return
	 * @throws Exception
	 */
	T exception(Exception e) throws Exception;
	
	/**
	 * 需要指定操作的集群
	 * @return MQCluster
	 */
	Cluster mqCluster();
}
