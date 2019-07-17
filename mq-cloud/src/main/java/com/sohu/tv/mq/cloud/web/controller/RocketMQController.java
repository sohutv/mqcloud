package com.sohu.tv.mq.cloud.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sohu.tv.mq.cloud.bo.NameServer;
import com.sohu.tv.mq.cloud.service.NameServerService;
import com.sohu.tv.mq.cloud.util.Jointer;
import com.sohu.tv.mq.cloud.util.Result;

/**
 * rocketmq
 * 
 * @author yongfeigao
 * @date 2018年10月23日
 */
@RestController
@RequestMapping("/rocketmq")
public class RocketMQController {
    
    @Autowired
    private NameServerService nameServerService;
    
    /**
     * name server地址
     * 
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/nsaddr-{clusterId}")
    public String nsaddr(@PathVariable int clusterId) throws Exception {
        Result<List<NameServer>> result = nameServerService.query(clusterId);
        return Jointer.BY_SEMICOLON.join(result.getResult(), ns -> ns.getAddr());
    }
}
