package com.sohu.tv.mq.cloud.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.validation.Valid;

import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.admin.TopicOffset;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.body.ProducerConnection;
import org.apache.rocketmq.common.protocol.route.QueueData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.AuditAssociateProducer;
import com.sohu.tv.mq.cloud.bo.AuditTopic;
import com.sohu.tv.mq.cloud.bo.AuditTopicUpdate;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserConsumer;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.AuditService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.ConsumerService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.service.UserConsumerService;
import com.sohu.tv.mq.cloud.service.UserProducerService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.service.VerifyDataService;
import com.sohu.tv.mq.cloud.util.FreemarkerUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.AssociateProducerParam;
import com.sohu.tv.mq.cloud.web.controller.param.TopicParam;
import com.sohu.tv.mq.cloud.web.vo.TopicRoute;
import com.sohu.tv.mq.cloud.web.vo.TopicRouteVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
/**
 * topic接口
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

    /**
     * 更新Topic路由
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="/updateRoute", method=RequestMethod.POST)
    public Result<?> updateRoute(UserInfo userInfo, @RequestParam("tid") int tid, 
            @RequestParam("queueNum") int queueNum,
            @RequestParam("info") String info) throws Exception {
        //校验审核记录是否重复
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
        if(result.isOK()) {
            String topicTip = " topic:<b>" + topicResult.getResult().getName() + "</b> 队列:<b>" + topicResult.getResult().getQueueNum() 
                    + "</b> 修改为:<b>" + queueNum + "</b>";
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.UPDATE_TOPIC, topicTip);
        }
        
        return Result.getWebResult(result);
    }
    
    /**
     * 获取topic路由
     * @return
     * @throws Exception
     */
    @RequestMapping("/{tid}/route")
    public String route(UserInfo userInfo, @PathVariable long tid, Map<String, Object> map) throws Exception {
        String view = viewModule() + "/route";
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if(topicResult.isNotOK()) {
            setResult(map, topicResult);
            return view;
        }
        Topic topic = topicResult.getResult();
        TopicRouteData topicRouteData = topicService.route(topic);
        List<TopicRoute> list = new ArrayList<TopicRoute>();
        int queueNum = 0;
        for(QueueData queueData : topicRouteData.getQueueDatas()) {
            TopicRoute topicRoute = new TopicRoute();
            if(queueNum == 0) {
                queueNum = queueData.getWriteQueueNums();
            }
            BeanUtils.copyProperties(queueData, topicRoute);
            list.add(topicRoute);
        }
        TopicRouteVO topicRouteVO = new TopicRouteVO();
        topicRouteVO.setQueueNum(queueNum);
        topicRouteVO.setTopic(topic);
        topicRouteVO.setTopicRouteList(list);
        if(userInfo.getUser().isAdmin()) {
            topicRouteVO.setOwn(true);
        } else {
            Result<List<UserProducer>> result = userProducerService.queryUserProducer(userInfo.getUser().getId(), tid);
            if(result.isOK()) {
                topicRouteVO.setOwn(true);
            }
        }
        setResult(map, topicRouteVO);
        return view;
    }
    
    /**
     * 获取topic各个队列状态
     * @return
     * @throws Exception
     */
    @RequestMapping("/{tid}/produce/progress")
    public String produceProgress(UserInfo userInfo, @PathVariable long tid, Map<String, Object> map) throws Exception {
        String view = viewModule() + "/produceProgress";
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if(topicResult.isNotOK()) {
            setResult(map, topicResult);
            return view;
        }
        Topic topic = topicResult.getResult();
        // 获取topic状态
        TopicStatsTable topicStatsTable = topicService.stats(topic);
        // 对topic状态进行排序
        HashMap<MessageQueue, TopicOffset> offsetTable = topicStatsTable.getOffsetTable();
        Map<String, TreeMap<MessageQueue, TopicOffset>> topicOffsetMap = new TreeMap<String, TreeMap<MessageQueue, TopicOffset>>();
        for(MessageQueue mq : offsetTable.keySet()) {
            TreeMap<MessageQueue, TopicOffset> offsetMap = topicOffsetMap.get(mq.getBrokerName());
            if(offsetMap == null) {
                offsetMap = new TreeMap<MessageQueue, TopicOffset>();
                topicOffsetMap.put(mq.getBrokerName(), offsetMap);
            }
            offsetMap.put(mq, offsetTable.get(mq));
        }
        setResult(map, topicOffsetMap);
        setResult(map, "topic", topic);
        return view;
    }
    
    /**
     * 新建topic
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="/add", method=RequestMethod.POST)
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
        audit.setInfo(topicParam.getInfo());
        // 构造topic审核记录
        AuditTopic auditTopic = new AuditTopic();
        BeanUtils.copyProperties(topicParam, auditTopic);
        // 保存记录
        Result<?> result = auditService.saveAuditAndTopic(audit, auditTopic);
        if(result.isOK()) {
            String tip = " topic:<b>" + topicParam.getName() + "</b>";
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.NEW_TOPIC, tip);
        }
        return Result.getWebResult(result);
    }
    
    /**
     * 获取topic列表
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/list")
    public Result<?> list(UserInfo userInfo) throws Exception {
        Result<List<Topic>> topicListResult = topicService.queryAllTopic();
        return Result.getWebResult(topicListResult);
    }
    
    /**
     * 关联生产者
     * @param topicParam
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="/associate", method = RequestMethod.POST)
    public Result<?> associate(UserInfo userInfo, @Valid AssociateProducerParam associateProducerParam) throws Exception {
        return associateUserProducer(userInfo,userInfo.getUser().getId(),associateProducerParam.getTid(),
                associateProducerParam.getProducer());
    }
    
    /**
     * 授权关联
     * 
     * @param userInfo
     * @param tid
     * @param uid
     * @param producer
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/auth/associate", method = RequestMethod.POST)
    public Result<?> authAssociate(UserInfo userInfo, @RequestParam("tid") long tid,
            @RequestParam("uid") long uid,
            @RequestParam("producer") String producer) throws Exception {
        if (tid < 1 || uid < 1 || producer == "") {
            return Result.getResult(Status.PARAM_ERROR);
        }
        return associateUserProducer(userInfo, uid, tid, producer);
    }
    
    /**
     * 复用之前的逻辑，
     * 
     * @param userInfo
     * @param uid
     * @param tid
     * @param producer
     * @return
     */
    private Result<?> associateUserProducer(UserInfo userInfo, long uid, long tid, String producer) {
        // 校验关联关系是否存在
        Result<?> isExist = verifyDataService.verifyUserProducerIsExist(uid, tid, producer);
        if (isExist.getStatus() != Status.OK.getKey()) {
            return isExist;
        }
        // 构建AuditAssociateProducer
        AuditAssociateProducer auditAssociateProducer = new AuditAssociateProducer();
        auditAssociateProducer.setProducer(producer);
        auditAssociateProducer.setTid(tid);
        auditAssociateProducer.setUid(uid);
        // 构建Audit
        Audit audit = new Audit();
        audit.setType(TypeEnum.ASSOCIATE_PRODUCER.getType());
        audit.setStatus(Audit.StatusEnum.INIT.getStatus());
        audit.setUid(userInfo.getUser().getId());
        Result<Audit> result = auditService.saveAuditAndAssociateProducer(audit, auditAssociateProducer);
        if (result.isOK()) {
            // 根据申请的不同发送不同的消息
            String tip = null;
            if (userInfo.getUser().getId() == uid) {
                tip = getTopicTip(tid) + " producer:<b>" + producer + "</b>";
            } else {
                Result<User> userResult = userService.query(uid);
                if (userResult.isOK()) {
                    tip = getTopicTip(tid) + " producer:<b>" + producer + "</b>  user:<b>"
                            + (userResult.getResult().getName() == null ? userResult.getResult().getEmailName()
                                    : userResult.getResult().getName())
                            + "</b>";
                }
            }
            if (tip == null) {
                return Result.getResult(Status.EMAIL_SEND_ERR);
            }
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.ASSOCIATE_PRODUCER, tip);
        }
        return Result.getWebResult(result);
    }

    /**
     * 删除topic
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="/delete/{tid}", method=RequestMethod.POST)
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
        if(result.isOK()) {
            alertService.sendAuditMail(userInfo.getUser(), TypeEnum.DELETE_TOPIC, topicResult.getResult().getName());
        }
        return Result.getWebResult(result);
    }
    
    /**
     * 诊断链接
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/connection", method=RequestMethod.POST)
    public String connection(UserInfo userInfo, @RequestParam("topic") String topic,
            @RequestParam("cid") int cid,
            @RequestParam("group") String group,
            @RequestParam("type") int type, Map<String, Object> map) throws Exception {
        FreemarkerUtil.set("mqVersion", MQVersion.class, map);
        String view = viewModule() + "/connection";
        Cluster cluster = clusterService.getMQClusterById(cid);
        if(type == 1) {
            Result<ProducerConnection> result = userProducerService.examineProducerConnectionInfo(group, topic, cluster);
            setResult(map, result);
        } else {
            Result<ConsumerConnection> result = consumerService.examineConsumerConnectionInfo(group, cluster);
            setResult(map, result);
        }
        return view;
    }
    
    /**
     * 获取topic的提示信息
     * @param tid
     * @return
     */
    private String getTopicTip(long tid) {
        StringBuilder sb = new StringBuilder();
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if(topicResult.isOK()) {
            sb.append(" topic:<b>");
            sb.append(topicResult.getResult().getName());
            sb.append("</b>");
        }
        return sb.toString();
    }
    
    @Override
    public String viewModule() {
        return "topic";
    }
}
