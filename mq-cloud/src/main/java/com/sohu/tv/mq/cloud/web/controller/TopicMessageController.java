package com.sohu.tv.mq.cloud.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.admin.TopicOffset;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.cloud.bo.Audit;
import com.sohu.tv.mq.cloud.bo.Audit.TypeEnum;
import com.sohu.tv.mq.cloud.bo.AuditResendMessage;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.DecodedMessage;
import com.sohu.tv.mq.cloud.bo.MessageData;
import com.sohu.tv.mq.cloud.bo.MessageQueryCondition;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.UserProducer;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.AlertService;
import com.sohu.tv.mq.cloud.service.AuditService;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.MessageService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.service.UserProducerService;
import com.sohu.tv.mq.cloud.util.CompressUtil;
import com.sohu.tv.mq.cloud.util.FreemarkerUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.SplitUtil;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.MessageParam;
import com.sohu.tv.mq.cloud.web.vo.TraceViewVO;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import com.sohu.tv.mq.util.CommonUtil;

/**
 * topic消息
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月21日
 */
@Controller
@RequestMapping("/topic/message")
public class TopicMessageController extends ViewController {
    
    public static final int TIME_SPAN = 5 * 60 * 1000;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private TopicService topicService;
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private MQAdminTemplate mqAdminTemplate;
    
    @Autowired
    private UserProducerService userProducerService;
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private AlertService alertService;
    
    /**
     * 首页
     * @return
     * @throws Exception
     */
    @RequestMapping("/index")
    public String index(UserInfo userInfo, 
            @RequestParam("tid") int tid, 
            Map<String, Object> map) throws Exception {
        String view = viewModule() + "/index";
        // 获取topic
        Result<Topic> topicResult = topicService.queryTopic(tid);
        if(!topicResult.isOK()) {
            setResult(map, Result.getResult(Status.NO_RESULT));
            return view;
        }
        Topic topic = topicResult.getResult();
        MessageQueryCondition messageQueryCondition = new MessageQueryCondition();
        messageQueryCondition.setCid((int)topic.getClusterId());
        messageQueryCondition.setTopic(topic.getName());
        // 舍掉毫秒
        long now = System.currentTimeMillis() / 1000 * 1000;
        long startTime = now - TIME_SPAN;
        messageQueryCondition.setStart(startTime);
        messageQueryCondition.setEnd(now);
        setResult(map, messageQueryCondition);
        
        // 获取集群信息
        Cluster cluster = clusterService.getMQClusterById(topic.getClusterId());
        ClusterInfo clusterInfo = mqAdminTemplate.execute(new DefaultCallback<ClusterInfo>() {
            public ClusterInfo callback(MQAdminExt mqAdmin) throws Exception {
                return mqAdmin.examineBrokerClusterInfo();
            }
            public Cluster mqCluster() {
                return cluster;
            }
        });
        int brokerSize = clusterInfo.getBrokerAddrTable().size();
        setResult(map, "brokerSize", brokerSize);
        
        // 获取topic最大偏移量
        TopicStatsTable topicStatsTable = topicService.stats(cluster, topic.getName());
        if(topicStatsTable != null) {
            long maxOffset = 0;
            for(TopicOffset topicOffset : topicStatsTable.getOffsetTable().values()) {
                if(maxOffset < topicOffset.getMaxOffset()) {
                    maxOffset = topicOffset.getMaxOffset();
                }
            }
            messageQueryCondition.setMaxOffset(maxOffset);
        }
        
        // 设置是否是拥有者
        setOwner(userInfo, map, tid);
        setTraceEnabled(map, topic.traceEnabled());
        setResult(map, "cluster", cluster);
        return view;
    }
    
    /**
     * 搜索
     * @return
     * @throws Exception
     */
    @RequestMapping("/search")
    public String search(UserInfo userInfo, 
            HttpServletRequest request, 
            HttpServletResponse response, 
            @RequestParam("startTime") Long startTime,
            @RequestParam("endTime") Long endTime,
            @RequestParam("append") boolean append,
            @RequestParam(name="key", required=false) String key,
            @RequestParam(name="messageParam") String messageParam,
            Map<String, Object> map) throws Exception {
        String view = viewModule() + "/search";
        // 解析参数对象
        if(StringUtils.isEmpty(key)) {
            key = null;
        } else {
            key = key.trim();
        }
        MessageQueryCondition messageQueryCondition = parseParam(startTime, endTime, key, messageParam, append);
        if(messageQueryCondition == null) {
            setResult(map, Result.getResult(Status.PARAM_ERROR));
            return view;
        }
        // 消息查询
        Result<MessageData> result = messageService.queryMessage(messageQueryCondition, false);
        setResult(map, result);
        setTraceEnabled(map, messageQueryCondition.getTopic());
        return view;
    }
    
    /**
     * 根据msgId查询消息
     * @return
     * @throws Exception
     */
    @RequestMapping("/id/search")
    public String view(UserInfo userInfo, 
            HttpServletRequest request, 
            HttpServletResponse response, 
            @RequestParam("topic") String topic,
            @RequestParam("cid") int cid,
            @RequestParam(name="msgId") String msgId,
            Map<String, Object> map) throws Exception {
        String view = viewModule() + "/idSearch";
        Cluster cluster = clusterService.getMQClusterById(cid);
        msgId = msgId.trim();
        // 消息查询
        Result<?> result = messageService.queryMessage(cluster, topic, msgId);
        setResult(map, result);
        return view;
    }
    
    /**
     * key搜索
     * @return
     * @throws Exception
     */
    @RequestMapping("/key/search")
    public String keySearch(UserInfo userInfo, 
            HttpServletRequest request, 
            HttpServletResponse response, 
            @RequestParam("topic") String topic,
            @RequestParam("keyStartTime") Long beginTime,
            @RequestParam("keyEndTime") Long endTime,
            @RequestParam("cid") int cid,
            @RequestParam(name="msgKey") String msgKey,
            Map<String, Object> map) throws Exception {
        String view = viewModule() + "/keySearch";
        Cluster cluster = clusterService.getMQClusterById(cid);
        // 时间点
        if(beginTime == 0 || endTime == 0 || beginTime > endTime) {
            setResult(map, Result.getResult(Status.PARAM_ERROR));
        } else {
            msgKey = msgKey.trim();
            // 消息查询
            Result<List<DecodedMessage>> result = messageService.queryMessageByKey(cluster, topic, msgKey, beginTime, endTime);
            setResult(map, result);
            setTraceEnabled(map, topic);
        }
        return view;
    }
    
    private void setTraceEnabled(Map<String, Object> map, String topic) {
        setTraceEnabled(map, traceEnabled(topic));
   }
    
    private void setTraceEnabled(Map<String, Object> map, boolean traceEnabled) {
         setResult(map, "traceEnabled", traceEnabled);
    }
    
    private boolean traceEnabled(String topic) {
        Result<Topic> topicResult = topicService.queryTopic(topic);
        return topicResult.isOK() && topicResult.getResult().traceEnabled();
    }
    
    /**
     * trace搜索
     * @return
     * @throws Exception
     */
    @RequestMapping("/trace/search")
    public String traceSearch(UserInfo userInfo, 
            HttpServletRequest request, 
            HttpServletResponse response, 
            @RequestParam("topic") String topic,
            @RequestParam("traceStartTime") Long beginTime,
            @RequestParam("traceEndTime") Long endTime,
            @RequestParam(name="traceKey") String msgKey,
            Map<String, Object> map) throws Exception {
        String view = viewModule() + "/traceSearch";
        // 时间点
        if(beginTime == 0 || endTime == 0 || beginTime > endTime) {
            setResult(map, Result.getResult(Status.PARAM_ERROR));
        } else {
            msgKey = msgKey.trim();
            topic = CommonUtil.buildTraceTopic(topic);
            Result<Topic> topicResult = topicService.queryTopic(topic);
            if(topicResult.isNotOK()) {
                setResult(map, topicResult);
            } else {
                // 消息查询
                Cluster cluster = clusterService.getMQClusterById(topicResult.getResult().getClusterId());
                Result<List<DecodedMessage>> result = messageService.queryMessageByKey(cluster, topic, msgKey, beginTime, endTime);
                if (result.isEmpty()) {
                    setResult(map, null);
                } else {
                    Map<String, TraceViewVO> viewMap = messageService.groupTraceMessage(result.getResult(), msgKey);
                    setResult(map, viewMap);
                }
            }
        }
        return view;
    }
    
    /**
     * 组装参数
     * @param time
     * @param topic
     * @param key
     * @param cid
     * @return
     * @throws IOException 
     */
    private MessageQueryCondition parseParam(Long startTime, Long endTime, String key, String messageParam, 
            boolean append) throws IOException {
        if(endTime < startTime) {
            return null;
        }
        // 解析对象
        String json = CompressUtil.uncompress(messageParam);
        MessageQueryCondition messageQueryCondition = JSON.parseObject(json, MessageQueryCondition.class);
        if(append) {
            messageQueryCondition.prepareForSearch();
            return messageQueryCondition;
        }
        messageQueryCondition.reset();
        messageQueryCondition.setStart(startTime);
        messageQueryCondition.setEnd(endTime);
        if(StringUtils.isNotEmpty(key)) {
            messageQueryCondition.setKey(key);
        }
        return messageQueryCondition;
    }
    
    /**
     * 消息轨迹
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/track", method=RequestMethod.POST)
    public String track(UserInfo userInfo, @Valid MessageParam messageParam, Map<String, Object> map) 
            throws Exception {
        String view = viewModule() + "/track";
        Result<?> trackResult = messageService.track(messageParam);
        setResult(map, trackResult);
        FreemarkerUtil.set("splitUtil", SplitUtil.class, map);
        return view;
    }
    
    /**
     * 根据偏移量搜索
     * @return
     * @throws Exception
     */
    @RequestMapping("/offset/search")
    public String offsetSearch(UserInfo userInfo, 
            HttpServletRequest request, 
            HttpServletResponse response, 
            @RequestParam("offsetStart") Long offsetStart,
            @RequestParam("offsetEnd") Long offsetEnd,
            @RequestParam(name="offsetKey", required=false) String key,
            @RequestParam("append") boolean append,
            @RequestParam("toBrokerName") String toBrokerName,
            @RequestParam("toQueue") int toQueue,
            @RequestParam(name="messageParam", required=false) String messageParam,
            Map<String, Object> map) throws Exception {
        String view = viewModule() + "/offsetSearch";
        // 解析参数对象
        if(StringUtils.isEmpty(key)) {
            key = null;
        } else {
            key = key.trim();
        }
        MessageQueryCondition messageQueryCondition = parseParam(offsetStart, offsetEnd, key, messageParam, append);
        if(messageQueryCondition == null) {
            setResult(map, Result.getResult(Status.PARAM_ERROR));
            return view;
        }
        resetBrokerAndQueueCondition(toBrokerName, toQueue, messageQueryCondition);
        // 消息查询
        Result<MessageData> result = messageService.queryMessage(messageQueryCondition, true);
        setResult(map, result);
        setTraceEnabled(map, messageQueryCondition.getTopic());
        return view;
    }
    
    /**
     * 根据偏移量搜索某个topic
     * @return
     * @throws Exception
     */
    @RequestMapping("/topic/offset/search")
    public String topicOffsetSearch(UserInfo userInfo, 
            HttpServletRequest request, 
            HttpServletResponse response, 
            @RequestParam("toCid") int cid,
            @RequestParam("toTopic") String topic,
            @RequestParam("toStart") Long offsetStart,
            @RequestParam("toEnd") Long offsetEnd,
            @RequestParam(name="toKey", required=false) String key,
            @RequestParam("toAppend") boolean append,
            @RequestParam("toBrokerName") String toBrokerName,
            @RequestParam("toQueue") int toQueue,
            @RequestParam(name="toMessageParam", required=false) String messageParam,
            Map<String, Object> map) throws Exception {
        String view = viewModule() + "/topicOffsetSearch";
        MessageQueryCondition messageQueryCondition = null;
        if(StringUtils.isEmpty(messageParam)) {
            messageQueryCondition = new MessageQueryCondition();
            messageQueryCondition.setCid(cid);
            messageQueryCondition.setTopic(topic);
            messageQueryCondition.setStart(offsetStart);
            messageQueryCondition.setEnd(offsetEnd);
            messageQueryCondition.reset();
            if(StringUtils.isNotEmpty(key)) {
                messageQueryCondition.setKey(key);
            }
        } else {
            messageQueryCondition = parseParam(offsetStart, offsetEnd, key, messageParam, append);
        }
        if(messageQueryCondition == null) {
            setResult(map, Result.getResult(Status.PARAM_ERROR));
            return view;
        }
        resetBrokerAndQueueCondition(toBrokerName, toQueue, messageQueryCondition);
        messageQueryCondition.setTopic(topic);
        
        // 消息查询
        Result<MessageData> result = messageService.queryMessage(messageQueryCondition, true);
        setResult(map, result);
        return view;
    }
    
    /**
     * 重置broker搜索条件
     * 
     * @param brokerName
     * @param queueId
     * @param messageQueryCondition
     */
    private void resetBrokerAndQueueCondition(String brokerName, int queueId,
            MessageQueryCondition messageQueryCondition) {
        // broker和队列有可能每次都变，所以每次都需要重新赋值
        if (!brokerName.equals("all")) {
            messageQueryCondition.setBrokerName(brokerName);
        }
        if (queueId != -1 && queueId >= 0) {
            messageQueryCondition.setQueueId(queueId);
        }
    }
    
    /**
     * 消息重发审核
     * @param userInfo
     * @param tid
     * @param msgIds
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/resend")
    public Result<?> resend(UserInfo userInfo, 
            @RequestParam("tid") int tid,
            @RequestParam("msgIds") String msgIds) throws Exception {
        // 检测
        String[] msgIdArray = msgIds.split(",");
        if(msgIdArray.length == 0) {
            return Result.getResult(Status.PARAM_ERROR);
        }
        if(!isOwner(userInfo, tid)) {
            return Result.getResult(Status.PERMISSION_DENIED_ERROR);
        }
        // 构造审核记录
        Audit audit = new Audit();
        audit.setType(TypeEnum.RESEND_MESSAGE.getType());
        audit.setUid(userInfo.getUser().getId());
        // 构造消息记录
        List<AuditResendMessage> auditResendMessageList = new ArrayList<AuditResendMessage>();
        for(String msgId : msgIdArray) {
            AuditResendMessage auditResendMessage = new AuditResendMessage();
            auditResendMessage.setMsgId(msgId);
            auditResendMessage.setTid(tid);
            auditResendMessageList.add(auditResendMessage);
        }
        
        // 保存记录
        Result<?> result = auditService.saveAuditAndAuditResendMessage(audit, auditResendMessageList);
        // 发送提醒邮件
        if(result.isOK()) {
            Result<Topic> topicResult = topicService.queryTopic(tid);
            if(topicResult.isOK()) {
                String tip = " topic:<b>" + topicResult.getResult().getName() + "</b> 消息量:" + msgIdArray.length;
                alertService.sendAuditMail(userInfo.getUser(), TypeEnum.RESEND_MESSAGE, tip);
            }
        }
        return Result.getWebResult(result);
    }
    
    /**
     * 设置是否是拥有者
     * @param userInfo
     * @param map
     * @param tid
     */
    private void setOwner(UserInfo userInfo, Map<String, Object> map, int tid) {
        setResult(map, "owner", getOwner(userInfo, tid));
    }
    
    private boolean isOwner(UserInfo userInfo, int tid) {
        return 1 == getOwner(userInfo, tid);
    }
    
    private int getOwner(UserInfo userInfo, int tid) {
        int owner = 0;
        if(userInfo.getUser().isAdmin()) {
            owner = 1;
        } else {
            Result<List<UserProducer>> result = userProducerService.queryUserProducer(userInfo.getUser().getId(), tid);
            if(result.isOK()) {
                owner = 1;
            }
        }
        return owner;
    }
    
    @Override
    public String viewModule() {
        return "msg";
    }

}
