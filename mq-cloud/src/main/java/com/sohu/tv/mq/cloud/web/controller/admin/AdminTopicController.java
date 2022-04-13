package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.service.ClusterService;
import com.sohu.tv.mq.cloud.service.TopicManagerService;
import com.sohu.tv.mq.cloud.service.UserGroupService;
import com.sohu.tv.mq.cloud.service.UserService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.controller.param.ManagerParam;
import com.sohu.tv.mq.cloud.web.vo.TopicManagerInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 主题管理
 *
 * @author fengwang
 * @Description:
 * @date 2022年02月13日
 */
@Controller
@RequestMapping("/admin/topicManager")
public class AdminTopicController extends AdminViewController {

    @Autowired
    private TopicManagerService topicManagerService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private ClusterService clusterService;

    /**
     * @description: 获取主题管理页面列表
     * @param: * @param: param
     * @param: map
     * @return: java.lang.String
     * @author fengwang219475
     * @date: 2022/2/21 14:09
     */
    @RequestMapping("/list")
    public String list(ManagerParam param, Map<String, Object> map,
                       @Valid PaginationParam paginationParam, HttpServletRequest request) throws Exception {
        setView(map, "list");
        param.buildQueryStr();
        // 设置分页参数
        setPagination(map, paginationParam);
        Result<List<TopicManagerInfoVo>> listResult = topicManagerService.queryAndBuilderTopic(param, paginationParam);
        setResult(map, "listResult", listResult);
        setResult(map, "queryParams", param);
        return view();
    }

    /**
     * @description: 获取所有集群
     * @param: * @param:
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/21 14:09
     */
    @RequestMapping("/getAllCluser")
    public Result<?> queryAllCluser() throws Exception {
        return clusterService.queryAll();
    }

    /**
     * @description: 获取所有组织
     * @param: * @param:
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/21 14:09
     */
    @RequestMapping("/getAllGroup")
    public Result<?> queryAllGroup() throws Exception {
        return userGroupService.queryAll();
    }

    /**
     * @description: 关联生产者
     * @param: * @param: tid topic ID
     * @param: pNames 多个生产者名称拼接字符串
     * @param: userId 所属用户
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/21 14:10
     */
    @RequestMapping("/addProducer")
    public Result<?> addProducers(@RequestParam Long tid, String pNames, @RequestParam Long userId,HttpServletRequest request) throws Exception {
        return topicManagerService.addProducers(tid, pNames, userId,request);
    }

    /**
     * @description: 获取指定topic状态
     * @param: * @param: tid
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/21 14:11
     */
    @RequestMapping("/getTopicStat")
    public Result<?> getTopicStat(@RequestParam Long tid) throws Exception {
        return topicManagerService.getTopicState(tid);
    }

    /**
     * @description: 确认主题状态
     * @param: * @param: tid
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/22 15:46
     */
    @RequestMapping("/confirmStatus")
    public Result<?> confirmStatus(@RequestParam long tid,HttpServletRequest request) throws Exception {
        return topicManagerService.confirmStatus(tid,request);
    }

    @Override
    public String viewModule() {
        return "topicManager";
    }

}
