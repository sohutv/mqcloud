package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.bo.ConsumerStat;
import com.sohu.tv.mq.cloud.service.AlarmConfigService;
import com.sohu.tv.mq.cloud.service.ConsumerMonitorService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.vo.ConsumerMonitorVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @RequestMapping("/consumer")
    public String list(Map<String, Object> map, @Valid PaginationParam paginationParam) {
        setView(map, "consumer");
        ConsumerMonitorVO consumerMonitorVO = new ConsumerMonitorVO();

        // 获取数量
        Result<Integer> countResult = consumerMonitorService.queryConsumerStatCount();
        if (!countResult.isOK()) {
            return view();
        }
        Result.setResult(map, paginationParam);
        paginationParam.caculatePagination(countResult.getResult());
        // 消费状态获取
        List<ConsumerStat> list = consumerMonitorService.getConsumerStatInfo(paginationParam.getBegin(),
                paginationParam.getNumOfPage());
        if (list != null && list.size() > 0) {
            for (ConsumerStat cs : list) {
                if (StringUtils.isNotBlank(cs.getTopic())) {
                    cs.setConsumerLink(mqCloudConfigHelper.getTopicConsumeHrefLink(cs.getTopic(),
                            cs.getConsumerGroup()));
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
