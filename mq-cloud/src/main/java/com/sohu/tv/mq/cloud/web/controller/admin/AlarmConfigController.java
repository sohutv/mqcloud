package com.sohu.tv.mq.cloud.web.controller.admin;

import javax.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.service.AlarmConfigService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.AlarmConfigParam;

/**
 * 报警阈值
 * 
 * @Description:
 * @author zhehongyaun
 * @date 2018年9月26日
 */
@Controller
@RequestMapping("/admin/alarm/config")
public class AlarmConfigController extends AdminViewController {

    @Autowired
    private AlarmConfigService alarmConfigService;

    /**
     * 获取报警配置详情
     * 
     * @param map
     * @return
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
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
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> addUserAlarmConfig(@Valid AlarmConfigParam alarmConfigParam) {
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
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> deleteAlarmConfigByConsumer(@RequestParam("consumer") String consumer) {
        Result<?> deleteResult = alarmConfigService.deleteByConsumer(consumer);
        return deleteResult;
    }

    @Override
    public String viewModule() {
        return "alarm";
    }
}
