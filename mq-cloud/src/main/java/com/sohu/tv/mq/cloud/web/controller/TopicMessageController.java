package com.sohu.tv.mq.cloud.web.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSON;
import com.sohu.tv.mq.cloud.bo.MessageData;
import com.sohu.tv.mq.cloud.bo.MessageQueryCondition;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.service.MessageService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.util.CompressUtil;
import com.sohu.tv.mq.cloud.util.DateUtil;
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
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private TopicService topicService;
    
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
        calculateTime(messageQueryCondition, (System.currentTimeMillis() - MessageQueryCondition.TIME_SPAN) / 1000 * 1000);
        setResult(map, messageQueryCondition);
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
            @RequestParam("time") String time,
            @RequestParam("append") boolean append,
            @RequestParam(name="key", required=false) String key,
            @RequestParam(name="messageParam") String messageParam,
            Map<String, Object> map) throws Exception {
        String view = viewModule() + "/search";
        // 解析参数对象
        if(StringUtils.isEmpty(key)) {
            key = null;
        }
        MessageQueryCondition messageQueryCondition = parseParam(time, key, messageParam, append);
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
     * 组装参数
     * @param time
     * @param topic
     * @param key
     * @param cid
     * @return
     * @throws IOException 
     */
    private MessageQueryCondition parseParam(String time, String key, String messageParam, boolean append) 
            throws IOException {
        // 解析对象
        String json = CompressUtil.uncompress(messageParam);
        MessageQueryCondition messageQueryCondition = JSON.parseObject(json, MessageQueryCondition.class);
        if(append) {
            messageQueryCondition.prepareForSearch();
            return messageQueryCondition;
        }
        // 解析查询的时间
        long queryTime = 0;
        try {
            queryTime = DateUtil.getFormat(DateUtil.YMD_DASH_BLANK_HMS_COLON).parse(time).getTime();
        } catch (Exception e) {
            logger.error("time parse err:{}", time, e);
        }
        if(queryTime == 0) {
            return null;
        }
        messageQueryCondition.reset();
        calculateTime(messageQueryCondition, queryTime);
        if(StringUtils.isNotEmpty(key)) {
            messageQueryCondition.setKey(key);
        }
        return messageQueryCondition;
    }
    
    /**
     * 计算时间
     * @param messageQueryCondition
     * @param time
     */
    private void calculateTime(MessageQueryCondition messageQueryCondition, long time) {
        messageQueryCondition.setTime(time);
        messageQueryCondition.setStart(time - MessageQueryCondition.TIME_SPAN);
        long end = time + MessageQueryCondition.TIME_SPAN;
        // 舍掉毫秒
        long now = System.currentTimeMillis() / 1000 * 1000;
        if(end > now) {
            end = now;
        }
        messageQueryCondition.setEnd(end);
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
    
    @Override
    public String viewModule() {
        return "msg";
    }

}
