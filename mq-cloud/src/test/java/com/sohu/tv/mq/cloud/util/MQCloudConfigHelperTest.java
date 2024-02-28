package com.sohu.tv.mq.cloud.util;

import com.sohu.tv.mq.cloud.Application;
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

}