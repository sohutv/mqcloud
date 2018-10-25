package com.sohu.tv.mq.common;

/**
 * 警报器
 * @Description: 
 * @author yongfeigao
 * @date 2018年1月24日
 */
public interface Alerter {
    /**
     * 发送警报
     * @param info
     */
    public void alert(String info);
}
