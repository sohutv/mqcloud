package com.sohu.index.tv.mq.common;

import java.util.Map;

/**
 * Created by yijunzhang on 14-7-28.
 */
public interface ConsumerExecutor {

    /**
     * 订阅回调方法
     *
     * @return
     */
    void execute(Map<String, Object> messageMap) throws Exception;

}
