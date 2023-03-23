package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.ManagerParam;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.vo.TopicManagerInfoVo;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import com.sohu.tv.mq.util.JSONUtil;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.protocol.body.SubscriptionGroupWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 主题管理
 *
 * @author fengwang
 * @Description:
 * @date 2022年02月13日
 */
@Controller
@RequestMapping("/admin/topicManager")
public class AdminTopicController extends AdminViewController {

    @Autowired
    private TopicManagerService topicManagerService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private CommonConfigService commonConfigService;

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private UserConsumerService userConsumerService;

    /**
     * @description: 获取主题管理页面列表
     * @param: * @param: param
     * @param: map
     * @return: java.lang.String
     * @author fengwang219475
     * @date: 2022/2/21 14:09
     */
    @RequestMapping("/list")
    public String list(ManagerParam param, Map<String, Object> map,
                       @Valid PaginationParam paginationParam, HttpServletRequest request) throws Exception {
        setView(map, "list");
        param.buildQueryStr();
        // 设置分页参数
        setPagination(map, paginationParam);
        Result<List<TopicManagerInfoVo>> listResult = topicManagerService.queryAndBuilderTopic(param, paginationParam);
        setResult(map, "listResult", listResult);
        setResult(map, "queryParams", param);
        return view();
    }

    /**
     * @description: 获取所有集群
     * @param: * @param:
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/21 14:09
     */
    @RequestMapping("/getAllCluser")
    public Result<?> queryAllCluser() throws Exception {
        return clusterService.queryAll();
    }

    /**
     * @description: 获取所有组织
     * @param: * @param:
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/21 14:09
     */
    @RequestMapping("/getAllGroup")
    public Result<?> queryAllGroup() throws Exception {
        return userGroupService.queryAll();
    }

    /**
     * @description: 关联生产者
     * @param: * @param: tid topic ID
     * @param: pNames 多个生产者名称拼接字符串
     * @param: userId 所属用户
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/21 14:10
     */
    @RequestMapping("/addProducer")
    public Result<?> addProducers(@RequestParam Long tid, String pNames, @RequestParam Long userId,HttpServletRequest request) throws Exception {
        return topicManagerService.addProducers(tid, pNames, userId,request);
    }

    /**
     * @description: 获取指定topic状态
     * @param: * @param: tid
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/21 14:11
     */
    @RequestMapping("/getTopicStat")
    public Result<?> getTopicStat(@RequestParam Long tid) throws Exception {
        return topicManagerService.getTopicState(tid);
    }

    /**
     * @description: 确认主题状态
     * @param: * @param: tid
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/22 15:46
     */
    @RequestMapping("/confirmStatus")
    public Result<?> confirmStatus(@RequestParam long tid,HttpServletRequest request) throws Exception {
        return topicManagerService.confirmStatus(tid,request);
    }

    /**
     * 复制主题及其消费组
     *
     * @param tid
     * @param cid
     * @param request
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/copyTopic")
    public Result<?> copyTopic(UserInfo ui, @RequestParam String topic, @RequestParam Long cid, HttpServletRequest request) throws Exception {
        logger.info("user:{} copy topic:{} cid:{}", ui, topic, cid);
        // 查询topic
        Result<Topic> topicResult = topicService.queryTopic(topic);
        if (topicResult.isNotOK()) {
            return Result.getWebResult(topicResult);
        }
        Topic realTopic = topicResult.getResult();
        Cluster cluster = clusterService.getMQClusterById(cid);
        // 查询topic状态
        TopicStatsTable topicStatsTable = topicService.stats(cluster, topic);
        if (topicStatsTable != null) {
            return Result.getResult(Status.TOPIC_REPEAT);
        }
        // 创建topic
        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setReadQueueNums(realTopic.getQueueNum());
        topicConfig.setWriteQueueNums(realTopic.getQueueNum());
        topicConfig.setTopicName(realTopic.getName());
        if (realTopic.getOrdered() == AuditTopic.HAS_ORDER) {
            topicConfig.setOrder(true);
        }
        Result<?> result = topicService.createAndUpdateTopicOnCluster(cluster, topicConfig);
        if (result.isNotOK()) {
            return result;
        }
        // 查询消费组
        Result<List<Consumer>> consumerListResult = consumerService.queryByTid(realTopic.getId());
        if (consumerListResult.isNotOK()) {
            return Result.getWebResult(consumerListResult);
        }
        List<Consumer> consumerList = consumerListResult.getResult();
        // 创建消费组
        for (Consumer consumer : consumerList) {
            Result<?> consumerResult = userConsumerService.createAndUpdateConsumerOnCluster(cluster, consumer);
            if (consumerResult.isNotOK()) {
                logger.error("create consumer error, consumer:{}", consumer.getName());
                return consumerResult;
            }
        }
        return Result.getOKResult();
    }

    /**
     * 删除主题及其消费组
     *
     * @param tid
     * @param cid
     * @param request
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/deleteTopic")
    public Result<?> deleteTopic(UserInfo ui, @RequestParam String topic, @RequestParam Integer cid,
                                 @RequestParam(name = "destCid", required = false) Integer destCid,
                                 HttpServletRequest request) throws Exception {
        logger.info("user:{} delete topic:{} cid:{}", ui, topic, cid);
        // 查询topic
        Result<Topic> topicResult = topicService.queryTopic(topic);
        if (topicResult.isNotOK()) {
            return Result.getWebResult(topicResult);
        }
        Topic realTopic = topicResult.getResult();
        Cluster cluster = clusterService.getMQClusterById(cid);
        // 删除topic
        Result<?> result = topicService.deleteTopicOnCluster(cluster, realTopic.getName());
        if (result.isNotOK()) {
            return result;
        }
        // 查询消费组
        Result<List<Consumer>> consumerListResult = consumerService.queryByTid(realTopic.getId());
        if (consumerListResult.isNotOK()) {
            return Result.getWebResult(consumerListResult);
        }
        List<Consumer> consumerList = consumerListResult.getResult();
        // 删除消费组
        for (Consumer consumer : consumerList) {
            Result<?> consumerResult = consumerService.deleteConsumerOnCluster(cluster, consumer.getName());
            if (consumerResult.isNotOK()) {
                logger.error("delete consumer error, consumer:{}", consumer.getName());
                return consumerResult;
            }
        }
        // 更改topic集群归属
        if (destCid != null) {
            Topic dbTopic = new Topic();
            dbTopic.setId(realTopic.getId());
            dbTopic.setClusterId(destCid);
            Result<?> updateResult = topicService.updateDBTopic(dbTopic);
            if (updateResult.isNotOK()) {
                return Result.getWebResult(updateResult);
            }
        }
        return Result.getOKResult();
    }

    /**
     * 变更路由
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/change/route")
    public Result<?> changeRoute(UserInfo ui, @RequestParam String resource, @RequestParam Long cid, HttpServletRequest request) throws Exception {
        logger.info("user:{} change route:{} resource:{} cid:{}", ui, resource, cid);
        Result<CommonConfig> commonConfigResult = commonConfigService.queryByKey("clientGroupNSConfig");
        if (commonConfigResult.isNotOK()) {
            return Result.getWebResult(commonConfigResult);
        }
        CommonConfig commonConfig = commonConfigResult.getResult();
        Map<String, String> map = JSONUtil.parse(commonConfig.getValue(), Map.class);
        if (cid == -1) {
            String prev = map.remove(resource);
            if (prev == null) {
                return Result.getResult(Status.ROUTE_NOT_EXIST_ERROR);
            }
        } else {
            String prev = map.put(resource, String.valueOf(cid));
            if (prev != null) {
                return Result.getResult(Status.ROUTE_EXIST_ERROR);
            }
        }
        String value = JSONUtil.toJSONString(map);
        commonConfig.setValue(value);
        Result<?> result = commonConfigService.save(commonConfig);
        if (result.isNotOK()) {
            return Result.getWebResult(result);
        }
        logger.info("change route value from:{}, to:{}", commonConfig.getValue(), value);
        return Result.getOKResult();
    }

    /**
     * 查询mqcloud中topic的消费者
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/mqcloud/topic/consumer")
    public Result<List<Consumer>> topicConsumer(@RequestParam String topic) throws Exception {
        Result<Topic> topicResult = topicService.queryTopic(topic);
        Result<List<Consumer>> consumerResult = consumerService.queryByTid(topicResult.getResult().getId());
        if (consumerResult.isOK()) {
            Collections.sort(consumerResult.getResult(), (o1, o2) -> o1.getName().compareTo(o2.getName()));
        }
        return consumerResult;
    }

    /**
     * 查询集群中topic的消费者
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/cluster/topic/consumer")
    public Result<?> clusterTopicConsumer(UserInfo ui, @RequestParam String topic, @RequestParam Long cid, HttpServletRequest request) throws Exception {
        Result<List<Consumer>> consumerListResult = topicConsumer(topic);
        if (consumerListResult.isNotOK()) {
            return consumerListResult;
        }
        Cluster cluster = clusterService.getMQClusterById(cid);
        Result<SubscriptionGroupWrapper> result = consumerService.queryAllConsumer(cluster);
        if (result.isNotOK()) {
            return result;
        }
        Set<String> consumerSet = consumerListResult.getResult().stream().map(Consumer::getName).collect(Collectors.toSet());
        List<String> list = result.getResult().getSubscriptionGroupTable().keySet().stream()
                .filter(consumer -> consumerSet.contains(consumer))
                .sorted()
                .collect(Collectors.toList());
        if (list.size() == 0) {
            return Result.getResult(Status.NO_RESULT);
        }
        return Result.getResult(list);
    }

    @Override
    public String viewModule() {
        return "topicManager";
    }

}
