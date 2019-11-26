package com.sohu.tv.mq.cloud.web.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.util.Version;

/**
 * 首页
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月5日
 */
@Controller
@RequestMapping("/intro")
public class IntroController extends ViewController {

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @RequestMapping
    public String index(Map<String, Object> map) {
        setResult(map, "header", viewModule()+"/header");
        setView(map, "index");
        setResult(map, "version", Version.get());
        setResult(map, "clientArtifactId", mqCloudConfigHelper.getClientArtifactId());
        setResult(map, "producerClass", mqCloudConfigHelper.getProducerClass());
        setResult(map, "consumerClass", mqCloudConfigHelper.getConsumerClass());
        return view();
    }

    @Override
    public String viewModule() {
        return "intro";
    }
}
