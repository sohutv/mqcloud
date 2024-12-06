package com.sohu.tv.mq.cloud.common.model;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 获取所有消费者信息请求头
 *
 * @author yongfeigao
 * @date 2024年10月28日
 */
public class GetAllConsumerInfoRequestHeader implements CommandCustomHeader {

    private boolean excludeSystemGroup;

    @Override
    public void checkFields() throws RemotingCommandException {
        // To change body of implemented methods use File | Settings | File
        // Templates.
    }

    public boolean isExcludeSystemGroup() {
        return excludeSystemGroup;
    }

    public void setExcludeSystemGroup(boolean excludeSystemGroup) {
        this.excludeSystemGroup = excludeSystemGroup;
    }
}
