package com.sohu.index.tv.mq.common;

import java.util.Collection;
import java.util.Map;

/**
 * 批量处理接口
 * @Description: 
 * @author yongfeigao
 * @date 2018年1月19日
 */
public interface BatchConsumerExecutor {

    /**
     * 订阅回调方法
     *
     * @return
     */
    void execute(Collection<Map<String, Object>> collection) throws Exception;

}
