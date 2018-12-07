package com.sohu.tv.mq.cloud.mq;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sohu.tv.mq.cloud.bo.Cluster;
/**
 * 模板类，便于统一处理资源，异常，日志
 * @Description: 
 * @author yongfeigao
 * @date 2018年5月24日
 */
@Component
public class MQAdminTemplate {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
    @Autowired
    private GenericKeyedObjectPool<Cluster, MQAdminExt> mqPool;

	/**
	 * 执行操作
	 * @param callback
	 * @return
	 * @throws Exception
	 */
    public <T> T execute(MQAdminCallback<T> callback) {
        MQAdminExt mqAdmin = null;
		try {
		    // 获取mqAdmin实例
            mqAdmin = mqPool.borrowObject(callback.mqCluster());
		    if(mqAdmin == null) {
		        logger.warn("cluster:{} cannot get mqadmin!", callback.mqCluster());
		        return null;
		    }
		    // 触发回调
			T t = callback.callback(mqAdmin);
			return t;
		} catch (Exception e) {
		    try {
		        // 触发异常情况回调
                return callback.exception(e);
            } catch (Exception ex) {
                logger.warn("cluster:{} exception err:{}", callback.mqCluster(), ex.getMessage());
                return null;
            }
		} finally {
		    if(mqAdmin != null) {
		        try {
                    mqPool.returnObject(callback.mqCluster(), mqAdmin);
                } catch (Exception e) {
                    logger.warn("cluster:{} shutdown err:{}", callback.mqCluster(), e.getMessage());
                }
		    }
		}
	}
}
