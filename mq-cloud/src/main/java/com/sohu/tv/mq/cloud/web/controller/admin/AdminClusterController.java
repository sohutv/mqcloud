package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.Cluster;
import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.ClusterParam;
import com.sohu.tv.mq.cloud.web.vo.UserInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 集群
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月5日
 */
@Controller
@RequestMapping("/admin/cluster")
public class AdminClusterController extends AdminViewController {

    @Autowired
    private ClusterService clusterService;

    /**
     * 获取列表
     */
    @RequestMapping("/list")
    public String list(Map<String, Object> map) throws Exception {
        setView(map, "list");
        Result<List<Cluster>> listResult = clusterService.queryAll();
        setResult(map, listResult);
        return view();
    }

    /**
     * 新增cluster
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<?> addCluster(UserInfo userInfo, @Valid ClusterParam clusterParam, Map<String, Object> map)
            throws Exception {
        Cluster cluster = new Cluster();
        BeanUtils.copyProperties(clusterParam, cluster);
        Result<?> result = clusterService.save(cluster);
        return result;
    }

    /**
     * 更新状态
     */
    @ResponseBody
    @RequestMapping(value = "/update/status", method = RequestMethod.POST)
    public Result<?> updateStatus(@RequestParam("id") int id, @RequestParam("status") int status)
            throws Exception {
        return clusterService.updateStatus(id, status);
    }

    @Override
    public String viewModule() {
        return "cluster";
    }
}
