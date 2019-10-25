package com.sohu.index.tv.mq.common;

import java.util.Collection;
import java.util.Map;

/**
 * 批量处理接口
 * 此接口只试用与消息类型为Map，提供一个适用性更广泛的
 * @Description: 
 * @author yongfeigao
 * @date 2018年1月19日
 */
@Deprecated
public interface BatchConsumerExecutor {

    /**
     * 订阅回调方法
     *
     * @return
     */
    void execute(Collection<Map<String, Object>> collection) throws Exception;

}
