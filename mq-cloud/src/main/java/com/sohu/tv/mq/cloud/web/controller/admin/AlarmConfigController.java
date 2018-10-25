package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.AlarmConfig;
import com.sohu.tv.mq.cloud.bo.Topic;
import com.sohu.tv.mq.cloud.bo.User;
import com.sohu.tv.mq.cloud.bo.UserAlarmConfig;
import com.sohu.tv.mq.cloud.service.AlarmConfigService;
import com.sohu.tv.mq.cloud.service.TopicService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.web.controller.param.AlarmConfigParam;
import com.sohu.tv.mq.cloud.web.vo.AlarmConfigVO;

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

    @Autowired
    private UserService userService;

    @Autowired
    private TopicService topicService;

    /**
     * 获取预警配置型列表
     * 
     * @param map
     * @return
     */
    @RequestMapping("/list")
    public String list(Map<String, Object> map) {
        setView(map, "list");
        AlarmConfigVO alarmConfigVO = new AlarmConfigVO();
        setResult(map, alarmConfigVO);
        // 默认配置
        Result<List<AlarmConfig>> alarmConfigResult = alarmConfigService.queryByUid(0);// 默认配置为用户id为0的记录
        if (alarmConfigResult.isNotOK()) {
            return view();
        }
        if (alarmConfigResult.isNotEmpty()) {
            alarmConfigVO.setDefaultConfig(alarmConfigResult.getResult().get(0));
        }
        // 用户配置
        Result<List<AlarmConfig>> alarmConfigListResult = alarmConfigService.queryUserAlarmConfig();
        if (alarmConfigListResult.isNotOK() || alarmConfigListResult.getResult().isEmpty()) {
            return view();
        }

        List<UserAlarmConfig> userAlarmConfigList = new ArrayList<UserAlarmConfig>(
                alarmConfigListResult.getResult().size());
        Set<Long> idList = new HashSet<Long>();
        for (AlarmConfig alarmConfig : alarmConfigListResult.getResult()) {
            if (0 != alarmConfig.getUid()) {
                idList.add(alarmConfig.getUid());
            }
        }
        Result<List<User>> userListResult = userService.query(idList);
        if (userListResult.isNotOK()) {
            return view();
        }
        for (AlarmConfig alarmConfig : alarmConfigListResult.getResult()) {
            for (User user : userListResult.getResult()) {
                if (user.getId() == alarmConfig.getUid()) {
                    UserAlarmConfig userAlarmConfig = new UserAlarmConfig();
                    userAlarmConfigList.add(userAlarmConfig);
                    userAlarmConfig.setUser(user);
                    try {
                        BeanUtils.copyProperties(alarmConfig, userAlarmConfig);
                    } catch (Exception e) {
                        return view();
                    }
                    break;
                }
            }
        }
        alarmConfigVO.setUserAlarmConfig(userAlarmConfigList);
        return view();
    }

    /**
     * 获取报警配置详情
     * 
     * @param map
     * @return
     */
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Result<?> getAlarmConfigByID(@PathVariable long id) {
        Result<AlarmConfig> alarmConfigResult = alarmConfigService.queryByID(id);
        return alarmConfigResult;
    }

    /**
     * 更新报警配置
     * 
     * @param map
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> updateAlarmConfigByID(@Valid AlarmConfigParam alarmConfigParam) {
        // 校验忽略报警的主题是否存在
        Result<?> flag = veriftTopic(alarmConfigParam.getIgnoreTopic());
        if (flag.isNotOK()) {
            return flag;
        }
        AlarmConfig alarmConfig = new AlarmConfig();
        BeanUtils.copyProperties(alarmConfigParam, alarmConfig);
        Result<?> updateResult = alarmConfigService.updateByID(alarmConfig);
        return updateResult;
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
        // 校验忽略报警的主题是否存在
        Result<?> flag = veriftTopic(alarmConfigParam.getIgnoreTopic());
        if (flag.isNotOK()) {
            return flag;
        }
        Result<List<AlarmConfig>> alarmConfigListResult = alarmConfigService.queryByUid(alarmConfigParam.getUid());
        if (alarmConfigListResult.isNotOK()) {
            return alarmConfigListResult;
        }
        for (AlarmConfig AlarmConfig : alarmConfigListResult.getResult()) {
            if (AlarmConfig.getTopic().equals(alarmConfigParam.getTopic())) {
                return Result.getResult(Status.DB_DUPLICATE_KEY);
            }
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
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> deleteAlarmConfigByID(@PathVariable long id) {
        Result<?> deleteResult = alarmConfigService.deleteByID(id);
        return deleteResult;
    }

    /**
     * 校验忽略的报警是否存在
     * 
     * @param topic
     * @return
     */
    private Result<?> veriftTopic(String topic) {
        if (topic != "") {
            String[] topicArray = topic.split(",");
            for (int i = 0; i < topicArray.length; i++) {
                Result<Topic> topicResult = topicService.queryTopic(topicArray[i]);
                if (topicResult.isNotOK()) {
                    return topicResult;
                }
            }
        }
        return Result.getOKResult();
    }

    @Override
    public String viewModule() {
        return "alarm";
    }
}
