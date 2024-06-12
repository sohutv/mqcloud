package com.sohu.tv.mq.cloud.web.controller;

import com.sohu.tv.mq.cloud.bo.NameServer;
import com.sohu.tv.mq.cloud.bo.Proxy;
import com.sohu.tv.mq.cloud.service.NameServerService;
import com.sohu.tv.mq.cloud.service.ProxyService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.util.Jointer;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.util.MQProtocol;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private ProxyService proxyService;

    @Autowired
    private TopicService topicService;

    /**
     * name server地址
     *
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/nsaddr-{clusterId}")
    public String nsaddr(@PathVariable int clusterId,
                         @RequestParam(value = "clientGroup", required = false) String clientGroup,
                         @RequestParam(value = "protocol", required = false) Integer protocol) throws Exception {
        // 测试环境禁止访问线上sohu
        if (mqCloudConfigHelper.isTestOnlineSohu()) {
            throw new IllegalAccessException("test online sohu route is forbidden");
        }
        // 优先使用mqcloud配置的nsaddr
        if (StringUtils.isNotEmpty(clientGroup)) {
            if (mqCloudConfigHelper.getClientGroupNSConfig() != null) {
                String config = mqCloudConfigHelper.getClientGroupNSConfig().get(clientGroup);
                if (config != null) {
                    try {
                        // 如果是数字，表示是集群id
                        clusterId = Integer.parseInt(config);
                        return getNameServerAddr(clusterId);
                    } catch (NumberFormatException e) {
                        // 如果不是数字，检测是否是ns地址
                        if (config.contains(":")) {
                            return config;
                        } else if (config.startsWith("proxy-")) {
                            // 如果是proxy-开头，表示是proxy集群
                            String[] configArray = config.split("-");
                            return getProxyAddr(NumberUtils.toInt(configArray[1]));
                        }
                    }
                }
            }
        }
        // 判断是否是proxy-remoting协议
        if (protocol != null) {
            if (MQProtocol.isProxyRemoting(protocol)) {
                return getProxyAddr(clusterId);
            }
        }
        return getNameServerAddr(clusterId);
    }

    /**
     * 获取ns地址列表
     *
     * @param clusterId
     * @return
     */
    private String getNameServerAddr(int clusterId) {
        Result<List<NameServer>> result = nameServerService.query(clusterId);
        return Jointer.BY_SEMICOLON.join(result.getResult(), ns -> ns.getAddr());
    }

    /**
     * 获取proxy地址列表
     *
     * @param clusterId
     * @return
     */
    private String getProxyAddr(int clusterId) {
        Result<List<Proxy>> proxyListResult = proxyService.query(clusterId);
        return Jointer.BY_SEMICOLON.join(proxyListResult.getResult(), proxy -> proxy.getAddr());
    }

    /**
     * name server地址
     *
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/kv/config")
    public Result kvConfig(String addr) throws Exception {
        // 根据addr查询name server
        Result<NameServer> nameServerResult = nameServerService.query(addr);
        if (nameServerResult.isNotOK()) {
            return nameServerResult;
        }
        // 根据name server所属集群查询顺序topic kv配置
        NameServer nameServer = nameServerResult.getResult();
        String kvConfig = mqCloudConfigHelper.getOrderTopicKVConfig(String.valueOf(nameServer.getCid()));
        if (kvConfig == null) {
            return Result.getResult(Status.NO_RESULT);
        }
        // 获取顺序topic
        Result<List<String>> listResult = topicService.queryOrderedTopicList(nameServer.getCid());
        if (listResult.isEmpty()) {
            return listResult;
        }
        Map<String, String> orderTopicConfig = new HashMap<>();
        for (String topic : listResult.getResult()) {
            orderTopicConfig.put(topic, kvConfig);
        }
        return Result.getResult(orderTopicConfig);
    }
}
