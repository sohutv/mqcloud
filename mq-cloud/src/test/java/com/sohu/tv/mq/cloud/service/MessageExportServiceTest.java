package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.util.Result;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Auther: yongfeigao
 * @Date: 2023/9/25
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MessageExportServiceTest {

    @Autowired
    private MessageExportService messageExportService;

    @Test
    public void testExport() {
        long end = System.currentTimeMillis();
        long start = end - 10 * 60 * 1000;
        Result<?> result = messageExportService.export(0, "mqcloud5-test-topic", start, end);
        Assert.assertTrue(result.isOK());
    }

}