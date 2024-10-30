package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.service.TopicWarnConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * topic预警配置
 *
 * @author yongfeigao
 * @create 2024/10/12
 */
@Controller
@RequestMapping("/admin/topicWarnConfig")
public class AdminTopicWarnConfigController extends AdminViewController {

    @Autowired
    private TopicWarnConfigService topicWarnConfigService;

    @GetMapping("/list")
    public String list(Map<String, Object> map) {
        setView(map, "list");
        setResult(map, topicWarnConfigService.queryAll());
        return view();
    }

    @Override
    public String viewModule() {
        return "topicWarnConfig";
    }
}
