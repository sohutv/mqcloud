package com.sohu.tv.mq.cloud.web.controller;

import com.sohu.tv.mq.cloud.bo.NameServer;
import com.sohu.tv.mq.cloud.service.NameServerService;
import com.sohu.tv.mq.cloud.util.Jointer;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    /**
     * name server地址
     * 
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/nsaddr-{clusterId}")
    public String nsaddr(@PathVariable int clusterId, @RequestParam(value = "clientGroup", required = false) String clientGroup) throws Exception {
        // 优先使用mqcloud配置的nsaddr
        if (StringUtils.isNotEmpty(clientGroup) && mqCloudConfigHelper.getClientGroupNSConfig() != null) {
            String config = mqCloudConfigHelper.getClientGroupNSConfig().get(clientGroup);
            if (config != null) {
                try {
                    // 如果是数字，表示是集群id
                    clusterId = Integer.parseInt(config);
                } catch (NumberFormatException e) {
                    // 如果不是数字，检测是否是ns地址
                    if (config.contains(":")) {
                        return config;
                    }
                }
            }
        }
        Result<List<NameServer>> result = nameServerService.query(clusterId);
        return Jointer.BY_SEMICOLON.join(result.getResult(), ns -> ns.getAddr());
    }
}
