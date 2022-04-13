package com.sohu.tv.mq.cloud.common.model;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

public class GetBrokerStoreStatRequestHeader implements CommandCustomHeader {

    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
