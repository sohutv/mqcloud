package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.bo.ConsumerStat;
import com.sohu.tv.mq.cloud.service.AlarmConfigService;
import com.sohu.tv.mq.cloud.service.ConsumerMonitorService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.AlarmConfigParam;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.vo.ConsumerMonitorVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
public class AdminMonitorController extends AdminViewController {
    
    @Autowired
    private ConsumerMonitorService consumerMonitorService;
    
    @Autowired
    private AlarmConfigService alarmConfigService;
    
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

    /**
     * 获取报警配置详情
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/config/detail", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> getAlarmConfigByID(@RequestParam("consumer") String consumer) {
        Result<AlarmConfig> alarmConfigResult = alarmConfigService.queryByConsumer(consumer);
        return alarmConfigResult;
    }

    /**
     * 添加报警配置
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/config/add", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> addUserAlarmConfig(@Valid AlarmConfigParam alarmConfigParam) {
        if (alarmConfigParam.getConsumer() == null) {
            alarmConfigParam.setConsumer("");
        }
        AlarmConfig alarmConfig = new AlarmConfig();
        BeanUtils.copyProperties(alarmConfigParam, alarmConfig);
        Result<?> saveResult = alarmConfigService.save(alarmConfig);
        return saveResult;
    }

    /**
     * 删除用户报警配置
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/config/delete", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> deleteAlarmConfigByConsumer(@RequestParam("consumer") String consumer) {
        Result<?> deleteResult = alarmConfigService.deleteByConsumer(consumer);
        return deleteResult;
    }

    @Override
    public String viewModule() {
        return "monitor";
    }
}
