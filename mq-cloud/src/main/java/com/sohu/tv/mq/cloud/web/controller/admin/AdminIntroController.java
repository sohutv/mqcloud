package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;

/**
 * 首页
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月5日
 */
@Controller
@RequestMapping("/admin/intro")
public class AdminIntroController extends AdminViewController {

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @RequestMapping
    public String index(Map<String, Object> map) {
        setView(map, "index");
        setResult(map, "nameServerDomain", mqCloudConfigHelper.getDomain());
        return view();
    }

    @Override
    public String viewModule() {
        return "intro";
    }
}
