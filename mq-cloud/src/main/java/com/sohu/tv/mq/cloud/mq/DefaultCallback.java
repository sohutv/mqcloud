package com.sohu.tv.mq.cloud.mq;
/**
 * 默认不处理异常的回调类
 * @Description: 使用者使用此类，不用实现exception(Exception e)方法
 * @author yongfeigao
 * @date 2018年5月24日
 * @param <T>
 */
public abstract class DefaultCallback<T> implements MQAdminCallback<T> {

    @Override
    public T exception(Exception e) {
        return null;
    }

}
