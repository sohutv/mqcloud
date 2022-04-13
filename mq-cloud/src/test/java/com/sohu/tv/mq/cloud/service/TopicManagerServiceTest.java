package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.Application;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.dao.UserProducerDao;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.controller.param.ManagerParam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TopicManagerServiceTest {
    
    @Autowired
    private TopicManagerService topicManagerService;

    @Autowired
    private UserProducerDao userProducerDao;
    
    @Test
    public void testQuery() throws Exception {
        ManagerParam topicManagerParam = new ManagerParam();
        topicManagerParam.setGid(1L);
        PaginationParam param = new PaginationParam();
        param.setCurrentPage(1);
        param.setNumOfPage(10);
        param.caculatePagination(30);
        //topicManagerService.queryAndBuilderTopic(topicManagerParam);
    }

    @Test
    public void testH2InsertData(){
        List<UserProducer> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UserProducer userProducer = new UserProducer();
            userProducer.setProducer("name_1");
            userProducer.setUid(15);
            userProducer.setTid(9);
            list.add(userProducer);
        }
        Integer batchInsertCount = userProducerDao.batchInsert(list);
        System.out.println(batchInsertCount);
    }
}
