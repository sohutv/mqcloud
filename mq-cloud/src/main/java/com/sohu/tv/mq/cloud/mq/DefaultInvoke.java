package com.sohu.tv.mq.cloud.mq;

import org.apache.rocketmq.tools.admin.MQAdminExt;
/**
 * 使用此类，只回调，但是不返回结果
 * @Description: 
 * @author yongfeigao
 * @date 2018年6月29日
 */
public abstract class DefaultInvoke extends DefaultCallback<Void> {

    @Override
    public Void callback(MQAdminExt mqAdmin) throws Exception {
        invoke(mqAdmin);
        return null;
    }
    
    /**
     * 实现此方法即可
     * @param mqAdmin
     */
    public abstract void invoke(MQAdminExt mqAdmin) throws Exception; 
}
