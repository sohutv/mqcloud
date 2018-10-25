package com.sohu.tv.mq.cloud.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 首页
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月5日
 */
@Controller
@RequestMapping("/")
public class IndexController {

    @RequestMapping
    public String index() {
        return "forward:/user/topic";
    }
}
