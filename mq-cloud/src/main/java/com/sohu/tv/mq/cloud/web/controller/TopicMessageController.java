package com.sohu.tv.mq.cloud.web.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.bo.DecodedMessage;
import com.sohu.tv.mq.cloud.bo.MessageData;
import com.sohu.tv.mq.cloud.bo.MessageQueryCondition;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.mq.DefaultCallback;
import com.sohu.tv.mq.cloud.mq.MQAdminTemplate;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.MessageService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.util.CompressUtil;
import com.sohu.tv.mq.cloud.util.FreemarkerUtil;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.SplitUtil;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.MessageParam;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * topic消息
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月21日
 */
@Controller
@RequestMapping("/msg")
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
        ClusterInfo clusterInfo = mqAdminTemplate.execute(new DefaultCallback<ClusterInfo>() {
            public ClusterInfo callback(MQAdminExt mqAdmin) throws Exception {
                return mqAdmin.examineBrokerClusterInfo();
            }
            public Cluster mqCluster() {
                return clusterService.getMQClusterById(topic.getClusterId());
            }
        });
        int brokerSize = clusterInfo.getBrokerAddrTable().size();
        setResult(map, "brokerSize", brokerSize);
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
        }
        MessageQueryCondition messageQueryCondition = parseParam(startTime, endTime, key, messageParam, append);
        if(messageQueryCondition == null) {
            setResult(map, Result.getResult(Status.PARAM_ERROR));
            return view;
        }
        // 消息查询
        Result<MessageData> result = messageService.queryMessage(messageQueryCondition);
        setResult(map, result);
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
            // 消息查询
            Result<List<DecodedMessage>> result = messageService.queryMessageByKey(cluster, topic, msgKey, beginTime, endTime);
            setResult(map, result);
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
        if(endTime < startTime || startTime <= 0 || endTime <= 0) {
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
     * 死消息搜索
     * @return
     * @throws Exception
     */
    @RequestMapping("/dead/search")
    public String deadSearch(UserInfo userInfo, 
            HttpServletRequest request, 
            HttpServletResponse response, 
            @RequestParam("deadStartTime") Long startTime,
            @RequestParam("deadEndTime") Long endTime,
            @RequestParam("deadCid") int cid,
            @RequestParam("deadTopic") String topic,
            @RequestParam("deadAppend") boolean append,
            @RequestParam(name="deadMessageParam", required=false) String messageParam,
            Map<String, Object> map) throws Exception {
        String view = viewModule() + "/deadSearch";
        if(endTime < startTime || startTime <= 0 || endTime <= 0) {
            return null;
        }
        MessageQueryCondition messageQueryCondition = null;
        if(StringUtils.isEmpty(messageParam)) {
            messageQueryCondition = new MessageQueryCondition();
            messageQueryCondition.setCid(cid);
            messageQueryCondition.setTopic(topic);
            messageQueryCondition.setStart(startTime);
            messageQueryCondition.setEnd(endTime);
            messageQueryCondition.reset();
        } else {
            messageQueryCondition = parseParam(startTime, endTime, null, messageParam, append);
        }
        if(messageQueryCondition == null) {
            setResult(map, Result.getResult(Status.PARAM_ERROR));
            return view;
        }
        
        // 消息查询
        Result<MessageData> result = messageService.queryMessage(messageQueryCondition);
        setResult(map, result);
        return view;
    }
    
    @Override
    public String viewModule() {
        return "msg";
    }

}
