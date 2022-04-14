package com.sohu.tv.mq.cloud.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.service.UserProducerService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 生产者
 * 
 * @author yongfeigao
 * @date 2018年9月13日
 */
@Controller
@RequestMapping("/producer")
public class ProducerController extends ViewController {
    
    @Autowired
    private UserProducerService userProducerService;
    
    /**
     * 状况展示
     * 
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/stats")
    public String stats(UserInfo userInfo, @RequestParam("producer") String producer, Map<String, Object> map)
            throws Exception {
        return viewModule() + "/stats";
    }
    
    /**
     * 消费者列表
     * 
     * @param topicParam
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/list")
    public Result<?> list(UserInfo userInfo, @RequestParam("tid") int tid) throws Exception {
        Result<List<UserProducer>> listResult = userProducerService.queryUserProducerByTid(tid);
        if (listResult.isNotEmpty()) {
            // 去重
            List<UserProducer> userProducerList = listResult.getResult();
            Set<UserProducer> userProducerSet = new HashSet<>();
            for (UserProducer userProducer : userProducerList) {
                userProducerSet.add(userProducer);
            }
            return Result.getResult(userProducerSet);
        }
        return Result.getWebResult(listResult);
    }
    
    @Override
    public String viewModule() {
        return "producer";
    }
}
