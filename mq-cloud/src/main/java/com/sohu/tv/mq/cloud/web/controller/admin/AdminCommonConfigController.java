package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.CommonConfig;
import com.sohu.tv.mq.cloud.service.CommonConfigService;
import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.CommonConfigParam;

/**
 * 通用配置
 * 
 * @author yongfeigao
 * @date 2018年10月17日
 */
@Controller
@RequestMapping("/admin/config")
public class AdminCommonConfigController extends AdminViewController {

    @Autowired
    private CommonConfigService commonConfigService;
    
    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    /**
     * 获取配置列表
     * 
     * @param map
     * @return
     */
    @RequestMapping("/list")
    public String list(Map<String, Object> map) {
        setView(map, "list");
        Result<List<CommonConfig>> result = commonConfigService.query();
        setResult(map, result);
        return view();
    }

    /**
     * 更新配置
     * 
     * @param map
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Result<?> update(@Valid CommonConfigParam commonConfigParam) {
        CommonConfig commonConfig = new CommonConfig();
        BeanUtils.copyProperties(commonConfigParam, commonConfig);
        Result<?> updateResult = commonConfigService.save(commonConfig);
        if(updateResult.isOK()) {
            try {
                mqCloudConfigHelper.init();
            } catch (Exception e) {
                logger.error("update mqCloudConfigHelper err", e);
            }
        }
        return Result.getWebResult(updateResult);
    }

    @Override
    public String viewModule() {
        return "config";
    }
}
