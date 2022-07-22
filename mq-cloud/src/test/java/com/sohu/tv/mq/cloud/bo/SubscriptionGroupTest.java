package com.sohu.tv.mq.cloud.bo;

import com.sohu.tv.mq.util.JSONUtil;
import org.junit.Test;

public class SubscriptionGroupTest {

    @Test
    public void test() {
        SubscriptionGroup subscriptionGroup = SubscriptionGroup.buildMonitorSubscriptionGroup();
        System.out.println(JSONUtil.toJSONString(subscriptionGroup));
    }

}
