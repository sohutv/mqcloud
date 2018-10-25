package com.sohu.tv.mq.cloud.web.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sohu.tv.mq.cloud.web.vo.UserInfo;

/**
 * 生产者
 * 
 * @author yongfeigao
 * @date 2018年9月13日
 */
@Controller
@RequestMapping("/producer")
public class ProducerController extends ViewController {
    
    /**
     * 状况展示
     * 
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/stats")
    public String stats(UserInfo userInfo, @RequestParam("producer") String producer, Map<String, Object> map)
            throws Exception {
        return viewModule() + "/stats";
    }
    
    @Override
    public String viewModule() {
        return "producer";
    }
}
