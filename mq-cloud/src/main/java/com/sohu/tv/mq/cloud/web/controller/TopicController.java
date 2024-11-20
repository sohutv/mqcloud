package com.sohu.tv.mq.cloud.web.controller;

import com.sohu.tv.mq.cloud.bo.*;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.common.util.WebUtil;
import com.sohu.tv.mq.cloud.service.*;
import com.sohu.tv.mq.cloud.util.DateUtil;
import com.sohu.tv.mq.cloud.util.FreemarkerUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.TopicParam;
import com.sohu.tv.mq.cloud.web.controller.param.TopicWarnConfigParam;
import com.sohu.tv.mq.cloud.web.vo.BrokersQueueOffsetVO;
import com.sohu.tv.mq.cloud.web.vo.BrokersQueueOffsetVO.BrokerQueueOffset;
import com.sohu.tv.mq.cloud.web.vo.BrokersQueueOffsetVO.QueueOffset;
import com.sohu.tv.mq.cloud.web.vo.IpSearchResultVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import com.sohu.tv.mq.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.admin.TopicOffset;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.Connection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.ProducerConnection;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

/**
 * topic接口
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年6月12日
 */
@Controller
@RequestMapping("/topic")
public class TopicController extends ViewController {

    @Autowired
    private TopicService topicService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserConsumerService userConsumerService;

    @Autowired
    private UserProducerService userProducerService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private UserService userService;

    @Autowired
    private VerifyDataService verifyDataService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ProducerTotalStatService producerTotalStatService;

    @Autowired
    private ConsumerClientStatService consumerClientStatService;

    @Autowired
    private ClientConnectionService clientConnectionService;

    @Autowired
    private TopicWarnConfigService topicWarnConfigService;

    /**
     * 更新Topic路由
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/updateRoute", method = RequestMethod.POST)
    public Result<?> updateRoute(UserInfo userInfo, @RequestParam("tid") int tid,
            @RequestParam("queueNum") int queueNum,
            @RequestParam("info") String info) throws Exception {
        // 校验审核记录是否重复
        Result<?> isExist = verifyDataService.verifyUpdateTopicIsExist(tid, queueNum);
        if (isExist.isNotOK()) {
            return isExist;
        }
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        // 构造审核记录
        Audit audit = new Audit();
        audit.setType(TypeEnum.UPDATE_TOPIC.getType());
        audit.setStatus(Audit.StatusEnum.INIT.getStatus());
        audit.setUid(userInfo.getUser().getId());
        audit.setInfo(info);
        // 构造topic审核记录
        AuditTopicUpdate auditTopicUpdate = new AuditTopicUpdate();
        auditTopicUpdate.setTid(tid);
        auditTopicUpdate.setQueueNum(queueNum);
        // 保存记录
        Result<?> result = auditService.saveAuditAndTopicUpdate(audit, auditTopicUpdate);
        if (result.isOK()) {
            String topicTip = " topic:<b>" + topicResult.getResult().getName() + "</b> 队列:<b>"
                    + topicResult.getResult().getQueueNum()
                    + "</b> 修改为:<b>" + queueNum + "</b>";
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.UPDATE_TOPIC, topicTip);
        }

        return Result.getWebResult(result);
    }

    /**
     * 获取topic各个队列状态
     * 
     * @return
     * @throws Exception
     */
    @RequestMapping("/{tid}/produce/progress")
    public String produceProgress(UserInfo userInfo, @PathVariable long tid, Map<String, Object> map) throws Exception {
        String view = viewModule() + "/produceProgress";
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if (topicResult.isNotOK()) {
            setResult(map, topicResult);
            return view;
        }
        Topic topic = topicResult.getResult();
        // 获取topic状态
        TopicStatsTable topicStatsTable = topicService.stats(topic);
        // 对topic状态进行排序
        Map<MessageQueue, TopicOffset> offsetTable = topicStatsTable.getOffsetTable();
        BrokersQueueOffsetVO brokersQueueOffsetVO = toBrokersQueueOffsetVO(offsetTable);
        brokersQueueOffsetVO.setTopic(topic.getName());
        brokersQueueOffsetVO.setTopicId(topic.getId());
        setResult(map, brokersQueueOffsetVO);
        return view;
    }
    
    private BrokersQueueOffsetVO toBrokersQueueOffsetVO(Map<MessageQueue, TopicOffset> offsetTable) {
        // 合并&排序
        Map<String, TreeMap<MessageQueue, TopicOffset>> topicOffsetMap = new TreeMap<>();
        for (MessageQueue mq : offsetTable.keySet()) {
            topicOffsetMap.computeIfAbsent(mq.getBrokerName(), broker->{
                return new TreeMap<MessageQueue, TopicOffset>();
            }).put(mq, offsetTable.get(mq));
        }
        // 转换
        List<BrokerQueueOffset> brokerQueueOffsetList = new ArrayList<>();
        for(String broker : topicOffsetMap.keySet()) {
            BrokerQueueOffset brokerQueueOffset = new BrokerQueueOffset();
            brokerQueueOffset.setBroker(broker);
            brokerQueueOffset.setQueueOffsetList(toQueueOffsetList(topicOffsetMap.get(broker)));
            brokerQueueOffset.calculate();
            brokerQueueOffsetList.add(brokerQueueOffset);
        }
        BrokersQueueOffsetVO brokersQueueOffsetVO = new BrokersQueueOffsetVO();
        brokersQueueOffsetVO.setBrokerQueueOffsetList(brokerQueueOffsetList);
        return brokersQueueOffsetVO;
    }
    
    /**
     * 转换为 QueueOffset
     * @param offSetMap
     * @return
     */
    private List<QueueOffset> toQueueOffsetList(TreeMap<MessageQueue, TopicOffset> offSetMap) {
        List<QueueOffset> list = new ArrayList<>();
        for(MessageQueue messageQueue : offSetMap.keySet()) {
            TopicOffset topicOffset = offSetMap.get(messageQueue);
            QueueOffset queueOffset = new QueueOffset();
            queueOffset.setQueueId(messageQueue.getQueueId());
            queueOffset.setMaxOffset(topicOffset.getMaxOffset());
            queueOffset.setMinOffset(topicOffset.getMinOffset());
            queueOffset.setLastUpdateTimestamp(topicOffset.getLastUpdateTimestamp());
            list.add(queueOffset);
        }
        return list;
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String add(Map<String, Object> map) throws Exception {
        setView(map, "add", "生产消息");
        return view();
    }

    /**
     * 新建topic
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> add(UserInfo userInfo, @Valid TopicParam topicParam) throws Exception {
        logger.info("create topic, user:{} topic:{}", userInfo, topicParam);
        Result<?> isExist = verifyDataService.verifyAddTopicIsExist(topicParam);
        if (isExist.isNotOK()) {
            return isExist;
        }
        // 构造审核记录
        Audit audit = new Audit();
        audit.setType(TypeEnum.NEW_TOPIC.getType());
        audit.setStatus(Audit.StatusEnum.INIT.getStatus());
        audit.setUid(userInfo.getUser().getId());
        audit.setInfo(HtmlUtils.htmlEscape(topicParam.getInfo(), "UTF-8"));
        // 构造topic审核记录
        AuditTopic auditTopic = new AuditTopic();
        BeanUtils.copyProperties(topicParam, auditTopic);
        // 保存记录
        Result<?> result = auditService.saveAuditAndTopic(audit, auditTopic);
        if (result.isOK()) {
            String tip = " topic:<b>" + topicParam.getName() + "</b>";
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.NEW_TOPIC, tip);
        }
        return Result.getWebResult(result);
    }

    /**
     * 获取topic列表
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/list")
    public Result<?> list(UserInfo userInfo) throws Exception {
        Result<List<Topic>> topicListResult = topicService.queryNoneTraceableTopic();
        return Result.getWebResult(topicListResult);
    }

    /**
     * 删除topic
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/delete/{tid}", method = RequestMethod.POST)
    public Result<?> delete(UserInfo userInfo, @PathVariable long tid) throws Exception {
        Result<List<UserConsumer>> userConsumerListResult = userConsumerService.queryUserConsumer(tid);
        // 数据库异常，直接返回
        if (userConsumerListResult.getException() != null) {
            return Result.getWebResult(userConsumerListResult);
        }
        // topic还有人消费，提示
        if (userConsumerListResult.isNotEmpty()) {
            return Result.getResult(Status.DELETE_ERR_CONSUMER_EXIST_RESULT);
        }
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if (topicResult.isNotOK()) {
            return Result.getWebResult(topicResult);
        }
        // 验证删除请求是否重复
        Result<?> isExist = verifyDataService.verifyDeleteTopicIsExist(tid);
        if (isExist.isNotOK()) {
            return isExist;
        }
        logger.info("delete topic, user:{} topic:{}", userInfo, tid);

        // 构造审核记录
        Audit audit = new Audit();
        audit.setType(TypeEnum.DELETE_TOPIC.getType());
        audit.setUid(userInfo.getUser().getId());
        // 保存记录
        Result<?> result = auditService.saveAuditAndTopicDelete(audit, tid, topicResult.getResult().getName());
        if (result.isOK()) {
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.DELETE_TOPIC, topicResult.getResult().getName());
        }
        return Result.getWebResult(result);
    }

    /**
     * 更新topic trace
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/update/trace/{tid}", method = RequestMethod.POST)
    public Result<?> updateTrace(UserInfo userInfo, @PathVariable long tid,
            @RequestParam("traceEnabled") int traceEnabled) throws Exception {
        // 校验当前用户是否拥有权限
        Result<UserProducer> userProducerResult = userProducerService.findUserProducer(userInfo.getUser().getId(), tid);
        if (userProducerResult.isNotOK() && !userInfo.getUser().isAdmin()) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        // 校验topic是否存在
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if (topicResult.isNotOK()) {
            return Result.getWebResult(topicResult);
        }
        Topic topic = topicResult.getResult();
        // 校验是否需要修改
        if (topic.getTraceEnabled() == traceEnabled) {
            return Result.getResult(Status.NO_NEED_MODIFY_ERROR);
        }

        // 校验消费者是否还开启trace了
        Result<List<Consumer>> consumerList = consumerService.queryByTid(tid);
        if (consumerList.isNotEmpty()) {
            for (Consumer consumer : consumerList.getResult()) {
                if (consumer.traceEnabled()) {
                    return Result.getResult(Status.CONSUMER_TRACE_OPEN);
                }
            }
        }

        // 构造审核记录
        Audit audit = new Audit();
        audit.setType(TypeEnum.UPDATE_TOPIC_TRACE.getType());
        audit.setUid(userInfo.getUser().getId());
        // 保存记录
        Result<?> result = auditService.saveAuditAndTopicTrace(audit, tid, traceEnabled);
        if (result.isOK()) {
            String traceTip = "当前状态:" + getTraceTip(topic.getTraceEnabled()) + ",修改为:" + getTraceTip(traceEnabled);
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.UPDATE_TOPIC_TRACE, traceTip);
        }
        return Result.getWebResult(result);
    }

    private String getTraceTip(int traceEnabled) {
        if (traceEnabled == 1) {
            return "开启";
        }
        return "关闭";
    }

    /**
     * 更新topic 流量突增预警开关
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/update/trafficWarn/{tid}", method = RequestMethod.POST)
    public Result<?> updateTrafficWarn(UserInfo userInfo, @PathVariable long tid,
                                 @RequestParam("trafficWarnEnabled") int trafficWarnEnabled) throws Exception {
        // 校验topic是否存在
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if (topicResult.isNotOK()) {
            return Result.getWebResult(topicResult);
        }
        Topic topic = topicResult.getResult();
        // 校验是否需要修改
        if (topic.getTrafficWarnEnabled() == trafficWarnEnabled) {
            return Result.getResult(Status.NO_NEED_MODIFY_ERROR);
        }
        // 校验当前用户是否拥有权限
        Result<UserProducer> userProducerResult = userProducerService.findUserProducer(userInfo.getUser().getId(), tid);
        if (userProducerResult.isNotOK() && !userInfo.getUser().isAdmin()) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        // 构造审核记录
        Audit audit = new Audit();
        audit.setType(TypeEnum.UPDATE_TOPIC_TRAFFIC_WARN.getType());
        audit.setUid(userInfo.getUser().getId());
        // 保存记录
        Result<?> result = auditService.saveAuditAndTopicTrafficWarn(audit, tid, trafficWarnEnabled);
        if (result.isOK()) {
            String tip = "当前状态:" + getTraceTip(topic.getTrafficWarnEnabled()) + ",修改为:" + getTraceTip(trafficWarnEnabled);
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.UPDATE_TOPIC_TRAFFIC_WARN, tip);
        }
        return Result.getWebResult(result);
    }

    /**
     * 诊断链接
     * 
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/connection", method = RequestMethod.POST)
    public String connection(UserInfo userInfo, @RequestParam("topic") String topic,
            @RequestParam("cid") int cid,
            @RequestParam("group") String group,
            @RequestParam("type") int type, Map<String, Object> map) throws Exception {
        FreemarkerUtil.set("mqVersion", MQVersion.class, map);
        setResult(map, null);
        setResult(map, "client", group);
        String view = viewModule() + "/connection";
        Cluster cluster = clusterService.getMQClusterById(cid);
        HashSet<Connection> connectionSet = null;
        Integer clientType = ClientLanguage.PRODUCER_CLIENT_GROUP_TYPE;
        if (type == 1) {
            Result<List<UserProducer>> upListResult = userProducerService.queryUserProducer(group);
            if (upListResult.isEmpty()) {
                return view;
            }
            UserProducer up = upListResult.getResult().get(0);
            Result<ProducerConnection> result = userProducerService.examineProducerConnectionInfo(group, topic,
                    cluster, up.isProxyRemoting());
            if (result.isOK()) {
                connectionSet = result.getResult().getConnectionSet();
            }
            // trace topic生产者需要到proxy查询下连接
            if (cluster.isEnableTrace() && CommonUtil.isTraceTopicProducer(group)) {
                result = userProducerService.examineProducerConnectionInfo(group, topic, cluster, true);
                if (result.isOK()) {
                    if (connectionSet == null) {
                        connectionSet = result.getResult().getConnectionSet();
                    } else {
                        connectionSet.addAll(result.getResult().getConnectionSet());
                    }
                }
            }
        } else {
            Result<Consumer> consumerResult = consumerService.queryConsumerByName(group);
            if (consumerResult.isNotOK()) {
                return view;
            }
            Result<ConsumerConnection> result = consumerService.examineConsumerConnectionInfo(group, cluster,
                    consumerResult.getResult().isProxyRemoting());
            clientType = ClientLanguage.CONSUMER_CLIENT_GROUP_TYPE;
            if (result.isOK()) {
                connectionSet = result.getResult().getConnectionSet();
            }
        }
        if (connectionSet == null || connectionSet.isEmpty()) {
            return view;
        }
        // 替换为新客户端
        connectionSet = clientConnectionService.checkConnectVersion(connectionSet, group, clientType, cid);
        List<Connection> connList = new ArrayList<Connection>(connectionSet);
        Collections.sort(connList, new Comparator<Connection>() {
            public int compare(Connection o1, Connection o2) {
                return o1.getClientId().compareTo(o2.getClientId());
            }
        });
        setResult(map, connList);
        return view;
    }

    /**
     * 根据topic查询broker信息
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/brokerName/list", method = RequestMethod.GET)
    public Result<?> getQueueList(@RequestParam("topic") String topic,
            @RequestParam("clusterId") long clusterId) throws Exception {
        if (topic == "") {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Cluster cluster = clusterService.getMQClusterById(clusterId);
        if (cluster == null) {
            return Result.getResult(cluster);
        }
        TopicStatsTable topicStatsTable = topicService.stats(cluster, topic);
        if (topicStatsTable == null) {
            return Result.getResult(topicStatsTable);
        }
        Map<String, Long> brokerNameMap = new TreeMap<String, Long>();
        for (MessageQueue mq : topicStatsTable.getOffsetTable().keySet()) {
            String brokerName = mq.getBrokerName();
            TopicOffset topicOffset = topicStatsTable.getOffsetTable().get(mq);
            if (!brokerNameMap.containsKey(brokerName) || brokerNameMap.get(brokerName) < topicOffset.getMaxOffset()) {
                brokerNameMap.put(brokerName, topicOffset.getMaxOffset());
            }
        }
        return Result.getResult(brokerNameMap);
    }

    /**
     * 获取topic的提示信息
     * 
     * @param tid
     * @return
     */
    public String getTopicTip(long tid) {
        StringBuilder sb = new StringBuilder();
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if (topicResult.isOK()) {
            sb.append(" topic:<b>");
            sb.append(topicResult.getResult().getName());
            sb.append("</b>");
        }
        return sb.toString();
    }

    /**
     * 更新Topic描述
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/update/info", method = RequestMethod.POST)
    public Result<?> updateTopicInfo(UserInfo userInfo, @RequestParam("tid") int tid,
            @RequestParam("info") String info) throws Exception {
        // 校验当前用户是否拥有权限
        Result<UserProducer> userProducerResult = userProducerService.findUserProducer(userInfo.getUser().getId(), tid);
        if (userProducerResult.isNotOK() && !userInfo.getUser().isAdmin()) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if (topicResult.isNotOK()) {
            return topicResult;
        }
        if (StringUtils.isBlank(info)) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        Topic topic = new Topic();
        topic.setId(tid);
        topic.setInfo(HtmlUtils.htmlEscape(info.trim(), "UTF-8"));
        Result<Integer> result = topicService.updateDBTopic(topic);
        logger.info(userInfo.getUser().getName() + " update topic info , tid:{}, info:{}, status:{}", tid, info,
                result.isOK());
        return Result.getWebResult(result);
    }

    /**
     * topic详情 只供管理员使用
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/detail")
    public String detail(HttpServletResponse response, HttpServletRequest request,
            UserInfo userInfo, @RequestParam("topic") String topic,
            @RequestParam(name = "consumer", required = false) String consumer) throws Exception {
        Result<Topic> topicResult = null;
        if (CommonUtil.isRetryTopic(topic)) {
            String topicConsumer = topic.substring(MixAll.RETRY_GROUP_TOPIC_PREFIX.length());
            Result<Consumer> consumerResult = consumerService.queryConsumerByName(topicConsumer);
            if (consumerResult.isNotOK()) {
                return Result.getWebResult(consumerResult).toJson();
            }
            topicResult = topicService.queryTopic(consumerResult.getResult().getTid());
        } else {
            topicResult = topicService.queryTopic(topic);
        }
        if (topicResult.isNotOK()) {
            return Result.getWebResult(topicResult).toJson();
        }
        if (consumer == null) {
            WebUtil.redirect(response, request, "/user/topic/" + topicResult.getResult().getId() + "/detail");
        } else {
            WebUtil.redirect(response, request,
                    "/user/topic/" + topicResult.getResult().getId() + "/detail?tab=consume&consumer=" + consumer);
        }
        return null;
    }

    @RequestMapping(value = "/ipSearch", method = RequestMethod.GET)
    public String ipSearch(Map<String, Object> map) throws Exception {
        setView(map, "ipSearch", "IP搜索");
        return view();
    }

    /**
     * 根据ip和时间查询topic
     * 
     * @param ip
     * @param time
     */
    @RequestMapping(value = "/search/ip")
    public String searchByIp(UserInfo userInfo,
            @RequestParam("ip") String ip,
            @RequestParam("date") String datePara,
            Map<String, Object> map) throws Exception {
        // 设置返回视图
        String view = viewModule() + "/ip";
        // 设置返回结果
        List<IpSearchResultVO> ipSearchResultVOList = new ArrayList<>();
        setResult(map, ipSearchResultVOList);
        // 参数解析
        if (ip != null) {
            ip = ip.trim();
            if (ip.length() == 0) {
                return view;
            }
        }
        Date date = DateUtil.getFormat(DateUtil.YMD_DASH).parse(datePara);
        // ip按生产者查询
        Result<List<String>> producerListResult = producerTotalStatService.queryProducerList(ip, date);
        if (producerListResult.isOK()) {
            List<String> producerList = producerListResult.getResult();
            for (String producer : producerList) {
                // 根据producer获取用户关联的topicId列表
                Result<List<Long>> topicIdListResult = userProducerService.findTopicIdList(userInfo.getUser(),
                        producer);
                saveIpSearchResultVO(ipSearchResultVOList, ip, producer, topicIdListResult, 1);
            }
        }
        // ip按消费者查询
        Result<List<String>> consumerListResult = consumerClientStatService.selectByDateAndClient(ip, date);
        if (consumerListResult.isOK()) {
            List<String> consumerList = consumerListResult.getResult(); // 这里的consumer同样是不重复的
            for (String consumer : consumerList) {
                // 根据consumer获取用户关联的topicId列表
                Result<List<Long>> topicIdListResult = userConsumerService.queryTopicId(userInfo.getUser(), consumer);
                saveIpSearchResultVO(ipSearchResultVOList, ip, consumer, topicIdListResult, 2);
            }
        }
        return view;
    }

    public void saveIpSearchResultVO(List<IpSearchResultVO> ipSearchResultVOList, String ip, String group,
            Result<List<Long>> topicIdListResult, int type) {
        if (topicIdListResult.isEmpty()) {
            return;
        }
        Result<List<Topic>> topicListResult = topicService.queryTopicList(topicIdListResult.getResult());
        if (topicListResult.isEmpty()) {
            return;
        }
        for (Topic topic : topicListResult.getResult()) {
            IpSearchResultVO ipSearchResultVO = null;
            if (type == 1) {
                ipSearchResultVO = new IpSearchResultVO(ip, null, group, topic);
            } else if (type == 2) {
                ipSearchResultVO = new IpSearchResultVO(ip, group, null, topic);
            }
            if (ipSearchResultVO != null) {
                ipSearchResultVOList.add(ipSearchResultVO);
            }
        }
    }

    /**
     * 获取http方式消费的consumer
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/httpConsumer")
    public Result<?> getHttpTopicConsumer() throws Exception {
        Result<List<TopicConsumer>> result = topicService.queryHttpConsumer();
        return Result.getWebResult(result);
    }

    /**
     * 获取http方式生产者
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/httpProducer")
    public Result<?> getHttpTopicProducer() throws Exception {
        Result<List<TopicProducer>> result = topicService.queryHttpProducer();
        return Result.getWebResult(result);
    }

    /**
     * 获取topic预警配置
     */
    @GetMapping("/warn/config/list/{tid}")
    public String warnConfigList(UserInfo userInfo, @PathVariable long tid, Map<String, Object> map) throws Exception {
        String view = viewModule() + "/warnConfigList";
        setResult(map, topicWarnConfigService.queryByTid(tid));
        return view;
    }

    /**
     * 配置topic预警
     */
    @ResponseBody
    @PostMapping("/warn/config/add")
    public Result<Integer> warnConfigAdd(UserInfo userInfo, @Valid TopicWarnConfigParam topicWarnConfigParam) throws Exception {
        logger.info("add topic warn config, user:{} topicWarnConfigParam:{}", userInfo, topicWarnConfigParam);
        if (hasPermission(userInfo, topicWarnConfigParam.getTid()).isNotOK()) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        TopicWarnConfig topicWarnConfig = new TopicWarnConfig();
        BeanUtils.copyProperties(topicWarnConfigParam, topicWarnConfig);
        return Result.getWebResult(topicWarnConfigService.save(topicWarnConfig));
    }

    /**
     * 删除topic预警
     */
    @ResponseBody
    @PostMapping("/warn/config/delete/{id}")
    public Result<?> warnConfigDelete(UserInfo userInfo, @PathVariable long id) throws Exception {
        logger.info("delete topic warn config, user:{} id:{}", userInfo, id);
        Result<TopicWarnConfig> result = topicWarnConfigService.queryById(id);
        if (result.isNotOK()) {
            return Result.getWebResult(result);
        }
        if (hasPermission(userInfo, result.getResult().getTid()).isNotOK()) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        return Result.getWebResult(topicWarnConfigService.delete(id));
    }

    /**
     * 更新topic预警
     */
    @ResponseBody
    @PostMapping("/warn/config/update/enable/{id}")
    public Result<?> warnConfigDelete(UserInfo userInfo, @PathVariable long id, int enable) throws Exception {
        logger.info("update topic warn config, user:{} id:{} enable:{}", userInfo, id, enable);
        Result<TopicWarnConfig> result = topicWarnConfigService.queryById(id);
        if (result.isNotOK()) {
            return Result.getWebResult(result);
        }
        if (hasPermission(userInfo, result.getResult().getTid()).isNotOK()) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        return Result.getWebResult(topicWarnConfigService.updateEnabled(id, enable));
    }

    /**
     * 获取topic预警
     */
    @ResponseBody
    @GetMapping("/warn/config/{id}")
    public Result<?> warnConfig(@PathVariable long id) throws Exception {
        return Result.getWebResult(topicWarnConfigService.queryById(id));
    }

    /**
     * 是否有权限
     */
    private Result hasPermission(UserInfo userInfo, long tid) {
        // 非admin用户需要校验是否有权限
        if (!userInfo.getUser().isAdmin()) {
            Result<UserProducer> userProducerResult = userProducerService.findUserProducer(userInfo.getUser().getId(),tid);
            if (userProducerResult.isNotOK()) {
                return Result.getResult(Status.PERMISSION_DENIED_ERROR);
            }
        }
        return Result.getOKResult();
    }

    @Override
    public String viewModule() {
        return "topic";
    }
}
