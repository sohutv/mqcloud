package com.sohu.tv.mq.cloud.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.common.util.WebUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.UserWarn;
import com.sohu.tv.mq.cloud.bo.UserWarnCount;
import com.sohu.tv.mq.cloud.bo.UserWarn.WarnType;
import com.sohu.tv.mq.cloud.util.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UserWarnServiceTest {

    @Autowired
    private UserWarnService userWarnService;

    @Test
    public void test() {
        String consumer = "test-consumer";
        StringBuilder content = new StringBuilder("详细如下:<br><br>");
        content.append("topic：<b>");
        content.append("a-b-topic");
        content.append("</b> 的消费者：<b>");
        content.append(consumer);
        content.append("</b> 检测到堆积，总堆积消息量：");
        content.append(WebUtil.countFormat(460932));
        content.append("，单个队列最大堆积消息量：");
        content.append(WebUtil.countFormat(30738));
        content.append("，消费滞后时间(相对于broker最新消息时间)：");
        content.append(WebUtil.timeFormat(351084000));
        UserWarn userWarn = new UserWarn();
        userWarn.setContent(content.toString());
        Result<UserWarn> rst = userWarnService.saveWarnContent(userWarn);
        Assert.assertEquals(true, rst.isOK());
        
        List<UserWarn> list = new ArrayList<>();
        int size = 10;
        for(int i = 0; i < size; ++i) {
            UserWarn uw = new UserWarn();
            uw.setWid(userWarn.getWid());
            uw.setResource(consumer);
            uw.setUid(i);
            uw.setType(WarnType.CONSUME_UNDONE.getType());
            list.add(uw);
        }
        Result<Integer> rst2 = userWarnService.batchSaveUserWarn(list);
        Assert.assertEquals(size, rst2.getResult().intValue());
    }
    
    @Test
    public void testBachSave() {
        String consumer = "test-consumer";
        StringBuilder content = new StringBuilder("详细如下:<br><br>");
        content.append("topic：<b>");
        content.append("a-b-topic");
        content.append("</b> 的消费者：<b>");
        content.append(consumer);
        content.append("</b> 检测到堆积，总堆积消息量：");
        content.append(1024);
        content.append("，单个队列最大堆积消息量：");
        content.append(10);
        content.append("，消费滞后时间(相对于broker最新消息时间)：");
        content.append(80);
        content.append("秒");
        Map<String, Object> map = new HashMap<>();
        map.put("resource", consumer);
        Result<?> rst = userWarnService.save(null, WarnType.CONSUME_UNDONE, map, content.toString());
        Assert.assertEquals(true, rst.isOK());
    }
    
    @Test
    public void testBachSave2() {
        String consumer = "test-consumer";
        StringBuilder content = new StringBuilder("详细如下:<br><br>");
        content.append("topic：<b>");
        content.append("a-b-topic");
        content.append("</b> 的消费者：<b>");
        content.append(consumer);
        content.append("</b> 检测到堆积，总堆积消息量：");
        content.append(1024);
        content.append("，单个队列最大堆积消息量：");
        content.append(10);
        content.append("，消费滞后时间(相对于broker最新消息时间)：");
        content.append(80);
        content.append("秒");
        Map<String, Object> map = new HashMap<>();
        map.put("resource", consumer);
        for(WarnType warnType : WarnType.values()) {
            List<Long> uidList = new ArrayList<>();
            uidList.add(1L);
            Result<?> rst = userWarnService.save(null, warnType, map, content.toString());
            Assert.assertEquals(true, rst.isOK());
        }
    }
    
    @Test
    public void testQuery() {
        long uid = 1;
        Result<Integer> rst = userWarnService.queryUserWarnCount(uid);
        Assert.assertEquals(1, rst.getResult().intValue());
        
        Result<List<UserWarn>> rst2 = userWarnService.queryUserWarnList(uid, 0, 1);
        Assert.assertEquals(1, rst2.getResult().size());
    }
    
    @Test
    public void testQueryDays() {
        long uid = 1;
        int days = 3;
        Result<List<UserWarnCount>> rst = userWarnService.queryUserWarnCount(uid, days);
        Assert.assertEquals(1, rst.getResult().size());
    }

    @Test
    public void testSave() {
        String consumer = "test-consumer";
        Map<String, Object> map = new HashMap<>();
        map.put("resource", consumer);
        map.put("topic", "test-topic");
        map.put("undoneMsgsTotal", 1024);
        map.put("undoneMsgsSingleMQ", 10);
        map.put("undoneMsgsDelayTime", 80);
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setId(1);
        user.setEmail("test@sohu-inc.com");
        users.add(user);
        Result<?> rst = userWarnService.save(users, WarnType.CONSUME_UNDONE, map, consumer + " alert");
        Assert.assertEquals(true, rst.isOK());
    }
}
