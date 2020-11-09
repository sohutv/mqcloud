package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.TopicTrafficWarnConfig;
import com.sohu.tv.mq.cloud.service.TopicTrafficStatService;
import com.sohu.tv.mq.cloud.service.TopicTrafficWarnConfigService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.vo.TopicTrafficWarnMonitorVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 流量监控
 * @author yongweizhao
 * @create 2020/9/25 10:24
 */
@Controller
@RequestMapping("/admin/trafficMonitor")
public class TrafficMonitorController extends AdminViewController {

    @Autowired
    private TopicTrafficWarnConfigService topicTrafficWarnConfigService;

    @Autowired
    private TopicTrafficStatService topicTrafficStatService;

    @RequestMapping("/topic")
    public String list(Map<String, Object> map) {
        setView(map, "topic");
        // 获取所有配置,从中解析出默认配置和自定义配置
        TopicTrafficWarnMonitorVO topicTrafficWarnMonitorVO = new TopicTrafficWarnMonitorVO();
        Result<List<TopicTrafficWarnConfig>> result = topicTrafficWarnConfigService.queryAll();
        if (result.isEmpty()) {
            return view();
        }
        List<TopicTrafficWarnConfig> list = result.getResult();
        Iterator<TopicTrafficWarnConfig> iterator = list.iterator();
        while (iterator.hasNext()) {
            TopicTrafficWarnConfig topicTrafficWarnConfig = iterator.next();
            if (StringUtils.isBlank(topicTrafficWarnConfig.getTopic())) {
                topicTrafficWarnMonitorVO.setDefaultConfig(topicTrafficWarnConfig);
                iterator.remove();
                break;
            }
        }
        if (list.size() > 0) {
            topicTrafficWarnMonitorVO.setCustomConfigList(list);
        }
        setResult(map, topicTrafficWarnMonitorVO);
        return view();
    }

    /**
     * 获取配置详情
     * @param topicName
     * @return
     */
    @RequestMapping(value = "/config/detail", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> getWarnConfig(@RequestParam("topicName") String topicName) {
        Result<TopicTrafficWarnConfig> result = topicTrafficWarnConfigService.query(topicName);
        return Result.getWebResult(result);
    }

    /**
     * 添加配置
     * @return
     */
    @RequestMapping(value = "/config/add", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> addWarnConfig(TopicTrafficWarnConfig topicTrafficWarnConfig) {
        Result<Integer> saveResult = topicTrafficWarnConfigService.save(topicTrafficWarnConfig);
        return Result.getWebResult(saveResult);
    }

    /**
     * 删除配置
     * @return
     */
    @RequestMapping(value = "/config/delete", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> deleteWarnConfig(@RequestParam("topic") String topic) {
        Result<Integer> deleteResult = topicTrafficWarnConfigService.delete(topic);
        return Result.getWebResult(deleteResult);
    }

    /**
     * 获取所有开启了流量预警功能topic列表
     * @return
     */
    @RequestMapping(value = "/topic/enabled", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> getEnabledTopicList() {
        List<Topic> topicList = topicTrafficStatService.queryTrafficWarnEnabledTopicList();
        return Result.getResult(topicList);
    }

    @Override
    public String viewModule() {
        return "trafficWarn";
    }
}
