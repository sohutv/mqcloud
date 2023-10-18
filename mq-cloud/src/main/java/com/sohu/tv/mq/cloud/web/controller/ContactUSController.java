package com.sohu.tv.mq.cloud.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;

/**
 * 联系我们
 * 
 * @author yongfeigao
 * @date 2018年10月15日
 */
@Controller
@RequestMapping("/contact")
public class ContactUSController extends ViewController {

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    /**
     * 获取配置信息
     * @return
     * @throws Exception
     */
    @RequestMapping
    public String index(Map<String, Object> map) throws Exception {
        setView(map, "index", "联系我们");
        List<Map<String, String>> operatorList = mqCloudConfigHelper.getOperatorContact();
        if(operatorList != null) {
            setResult(map, "operatorList", operatorList);
        }
        String specialThx = mqCloudConfigHelper.getSpecialThx();
        if(specialThx != null) {
            setResult(map, "specialThx", specialThx);
        }
        return view();
    }
    
    @Override
    public String viewModule() {
        return "contact";
    }

}
