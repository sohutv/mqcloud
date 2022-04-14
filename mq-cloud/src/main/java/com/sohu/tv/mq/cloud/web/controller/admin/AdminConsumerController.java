package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.service.ConsumerManagerService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.ManagerParam;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: TODO
 * @date 2022/2/28 15:07
 */
@Controller
@RequestMapping("/admin/consumer")
public class AdminConsumerController extends AdminViewController{

    @Autowired
    private ConsumerManagerService consumerManagerService;


    @RequestMapping("/list")
    public String list(ManagerParam param, Map<String, Object> map,
                       @Valid PaginationParam paginationParam, HttpServletRequest request) throws Exception {
        setView(map, "list");
        param.buildQueryStr();
        // 设置分页参数
        setPagination(map, paginationParam);
        Result<?> listResult = consumerManagerService.queryAndFilterConsumerList(param, paginationParam);
        setResult(map, "listResult", listResult);
        setResult(map, "queryParams", param);
        return view();
    }

    @RequestMapping("/getAttributes")
    public Result<?> getAttributes(@RequestParam long cid){
        return consumerManagerService.getConsumerAttribute(cid);
    }


    @RequestMapping("/updateAttributes")
    public Result<?> getAttributes(@RequestParam long cid,@RequestParam int consumeWay,HttpServletRequest request){
        return consumerManagerService.editConsumerType(cid,consumeWay,request);
    }

    @RequestMapping("/getConsumerState")
    public Result<?> getConsumerState(@RequestParam long cid,@RequestParam long tid){
        return consumerManagerService.getConsumerState(cid,tid);
    }

    @Override
    public String viewModule() {
        return "consumer";
    }
}
