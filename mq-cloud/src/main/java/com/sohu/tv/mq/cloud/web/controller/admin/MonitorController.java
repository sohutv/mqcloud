package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.bo.ConsumerStat;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.service.AlarmConfigService;
import com.sohu.tv.mq.cloud.service.ConsumerMonitorService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.vo.ConsumerMonitorVO;

/**
 * 监控
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月5日
 */
@Controller
@RequestMapping("/admin/monitor")
public class MonitorController extends AdminViewController {
    
    @Autowired
    private ConsumerMonitorService consumerMonitorService;
    
    @Autowired
    private AlarmConfigService alarmConfigService;
    
    @Autowired
    private TopicService topicService;
    
    @RequestMapping("/consumer")
    public String list(Map<String, Object> map) {
        setView(map, "consumer");
        ConsumerMonitorVO consumerMonitorVO = new ConsumerMonitorVO();
        // 消费状态获取
        List<ConsumerStat> list = consumerMonitorService.getConsumerStatInfo();
        if (list != null && list.size() > 0) {
            Set<String> nameList = new HashSet<String>();
            for (ConsumerStat cs : list) {
                if (StringUtils.isNotBlank(cs.getTopic())) {
                    nameList.add(cs.getTopic());
                }
            }
            if (nameList.size() > 0) {
                Result<List<Topic>> result = topicService.queryTopicListByNameList(nameList);
                for (ConsumerStat cs : list) {
                    for (Topic t : result.getResult()) {
                        if (t.getName().equals(cs.getTopic())) {
                            cs.setTid(t.getId());
                            break;
                        }
                    }
                }
            }
        }
        consumerMonitorVO.setConsumerStat(list);
        
        // 报警全部配置
        Result<List<AlarmConfig>> alarmConfigListResult = alarmConfigService.queryAll();
        if (alarmConfigListResult.isNotOK() || alarmConfigListResult.getResult().isEmpty()) {
            return view();
        }
        List<AlarmConfig> alarmConfiglist = alarmConfigListResult.getResult();
        Iterator<AlarmConfig> iterator = alarmConfiglist.iterator();
        while (iterator.hasNext()) {
            AlarmConfig alarmConfig = iterator.next();
            if (StringUtils.isBlank(alarmConfig.getConsumer())) {
                consumerMonitorVO.setDefaultConfig(alarmConfig);
                iterator.remove();
                break;
            }
        }
        if (alarmConfiglist.size() > 0) {
            consumerMonitorVO.setAlarmConfig(alarmConfiglist);
        }
        setResult(map, consumerMonitorVO);
        return view();
    }

    @Override
    public String viewModule() {
        return "monitor";
    }
}
