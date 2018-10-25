package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sohu.tv.mq.cloud.bo.ClientVersion;
import com.sohu.tv.mq.cloud.service.ClientVersionService;
import com.sohu.tv.mq.cloud.util.Result;
/**
 * 客户端信息
 * @Description: 
 * @author yongfeigao
 * @date 2018年8月31日
 */
@Controller
@RequestMapping("/admin/client")
public class AdminClientController extends AdminViewController {
    
    @Autowired
    private ClientVersionService clientVersionService;
    
    /**
     * 获取notice列表
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    public String list(Map<String, Object> map) throws Exception {
        setView(map, "list");
        Result<List<ClientVersion>> clientVersionListResult = clientVersionService.queryAll();
        setResult(map, clientVersionListResult);
        return view();
    }
    
    @Override
    public String viewModule() {
        return "client";
    }

}
