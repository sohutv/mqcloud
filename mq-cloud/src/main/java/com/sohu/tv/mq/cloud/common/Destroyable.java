package com.sohu.tv.mq.cloud.common;

/**
 * 可销毁的
 * 
 * @author yongfeigao
 * @date 2018年9月12日
 */
public interface Destroyable extends Comparable<Destroyable>{
    /**
     * 销毁
     * @throws Exception
     */
    public void destroy() throws Exception;
    
    /**
     * 销毁的顺序，顺序越小，越靠前销毁
     * @return
     */
    public int order();
}
