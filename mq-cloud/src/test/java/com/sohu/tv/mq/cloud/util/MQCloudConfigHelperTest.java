package com.sohu.tv.mq.cloud.util;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.common.model.BrokerStoreStat;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Auther: yongfeigao
 * @Date: 2023/12/1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MQCloudConfigHelperTest {

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Test
    public void test() {
        String producer = "mqcloud-json-test-topic-producer";
        Assert.assertTrue(mqCloudConfigHelper.isIgnoreErrorProducer(producer));
    }

    @Test
    public void testNeedWarn() {
        BrokerStoreStat brokerStoreStat = new BrokerStoreStat();
        brokerStoreStat.setClusterId(1);
        brokerStoreStat.setMax(501);
        brokerStoreStat.setPercent99(400);
        boolean needWarn = mqCloudConfigHelper.needWarn(brokerStoreStat);
        Assert.assertEquals(true, needWarn);

        brokerStoreStat.setMax(500);
        needWarn = mqCloudConfigHelper.needWarn(brokerStoreStat);
        Assert.assertEquals(false, needWarn);

        brokerStoreStat.setClusterId(2);
        brokerStoreStat.setMax(1000);
        brokerStoreStat.setPercent99(800);
        needWarn = mqCloudConfigHelper.needWarn(brokerStoreStat);
        Assert.assertEquals(false, needWarn);

        brokerStoreStat.setMax(1001);
        brokerStoreStat.setPercent99(800);
        needWarn = mqCloudConfigHelper.needWarn(brokerStoreStat);
        Assert.assertEquals(true, needWarn);

        brokerStoreStat.setMax(1000);
        brokerStoreStat.setPercent99(801);
        needWarn = mqCloudConfigHelper.needWarn(brokerStoreStat);
        Assert.assertEquals(true, needWarn);
    }

}