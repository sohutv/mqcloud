package com.sohu.tv.mq.cloud.bo;

import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class SubscriptionGroupTest {

    @Test
    public void test() {
        SubscriptionGroup subscriptionGroup = SubscriptionGroup.buildMonitorSubscriptionGroup();
        System.out.println(JSON.toJSONString(subscriptionGroup));
    }

}
