package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicTraffic;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.Result;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicTrafficServiceTest {

    @Autowired
    private TopicTrafficService topicTrafficService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TopicService topicService;

    @Test
    public void testSave() {
        TopicTraffic topicTraffic = new TopicTraffic();
        topicTraffic.setTid(1);
        topicTraffic.setSize(100);
        topicTraffic.setCount(3232);
        topicTraffic.setCreateTime(DateUtil.getFormatNow(DateUtil.HHMM));
        topicTrafficService.save(topicTraffic);
    }

    @Test
    public void testCollectTraffic() {
        topicTrafficService.collectTraffic(clusterService.getMQClusterById(1));
    }

    @Test
    public void testUpdateTopicDayTraffic() {
        Result<?> rst = topicTrafficService.updateTopicDayTraffic();
        Assert.assertNotNull(rst);
        List<Topic> topics = topicService.queryAllTopic().getResult();
        Date day1Ago = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        for (Topic topic : topics) {
            Date beginDate = day1Ago;
            compareData(topic.getId(), beginDate, day1Ago, topic.getSize1d());
            beginDate = new Date(beginDate.getTime() - 24 * 60 * 60 * 1000);
            compareData(topic.getId(), beginDate, day1Ago, topic.getSize2d());
            beginDate = new Date(beginDate.getTime() - 24 * 60 * 60 * 1000);
            compareData(topic.getId(), beginDate, day1Ago, topic.getSize3d());
            beginDate = new Date(beginDate.getTime() - 2 * 24 * 60 * 60 * 1000);
            compareData(topic.getId(), beginDate, day1Ago, topic.getSize5d());
            beginDate = new Date(beginDate.getTime() - 2 * 24 * 60 * 60 * 1000);
            compareData(topic.getId(), beginDate, day1Ago, topic.getSize7d());
        }
    }

    public void compareData(long id, Date begin, Date end, long expect) {
        Long count = topicTrafficService.queryTopicSummarySize(id, begin, end).getResult();
        Assert.assertEquals(count == null ? 0 : count.longValue(), expect);
    }

    @Test
    public void testImportData() throws IOException {
        FileReader fileReader = new FileReader(new File("/tmp/data.csv"));
        BufferedReader br = new BufferedReader(fileReader);
        List<TopicTraffic> topicTraffics = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] strings = line.split(",");
            TopicTraffic topicTraffic = new TopicTraffic();
            topicTraffic.setTid(Long.parseLong(strings[0]));
            topicTraffic.setCreateDate(DateUtil.parse(DateUtil.YMD_DASH, strings[1]));
            topicTraffic.setCreateTime(strings[2]);
            topicTraffic.setCount(Long.parseLong(strings[3]));
            topicTraffic.setSize(Long.parseLong(strings[4]));
            topicTraffics.add(topicTraffic);
            if (topicTraffics.size() >= 10000) {
                long start = System.currentTimeMillis();
                Result<Integer> result = topicTrafficService.batchInsert(topicTraffics);
                topicTraffics.clear();
                System.out.println(result.getResult()+",cost:" + (System.currentTimeMillis() - start) + "ms");
            }
        }
        if (topicTraffics.size() > 0) {
            long start = System.currentTimeMillis();
            Result<Integer> result = topicTrafficService.batchInsert(topicTraffics);
            System.out.println(result.getResult() + ",cost:" + (System.currentTimeMillis() - start) + "ms");
        }
        br.close();
    }
}
