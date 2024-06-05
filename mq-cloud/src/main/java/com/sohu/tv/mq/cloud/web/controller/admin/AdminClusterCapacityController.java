package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.service.ClusterCapacityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * 集群容量
 *
 * @author yongfeigao
 * @date 2024年5月29日
 */
@Controller
@RequestMapping("/admin/clusterCapacity")
public class AdminClusterCapacityController extends AdminViewController {

    @Autowired
    private ClusterCapacityService clusterCapacityService;

    @RequestMapping
    public String index(Map<String, Object> map) {
        setView(map, "index");
        setResult(map, clusterCapacityService.getClusterCapacity());
        return view();
    }

    @Override
    public String viewModule() {
        return "clusterCapacity";
    }
}
