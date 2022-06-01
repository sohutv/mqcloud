package com.sohu.tv.mq.cloud.web.controller.admin;

import com.sohu.tv.mq.cloud.bo.ClientLanguage;
import com.sohu.tv.mq.cloud.service.ClientConnectionService;
import com.sohu.tv.mq.cloud.task.TaskExecutor;
import com.sohu.tv.mq.cloud.web.controller.param.ManagerParam;
import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.cloud.web.vo.ClientLanguageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.sohu.tv.mq.cloud.util.Result;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author fengwang219475
 * @version 1.0
 * @description: 客户端链接管理
 * @date 2022/5/5 16:27
 */

@Controller
@RequestMapping("/admin/clientConnect")
public class AdminClientConnectionController extends AdminViewController {

    @Autowired
    private ClientConnectionService clientConnectionService;

    @Autowired
    private TaskExecutor taskExecutor;


    /**
     * @description: 多条件查询
     * @param: * @param: param
     * @param: map
     * @param: paginationParam
     * @return: java.lang.String
     * @author fengwang219475
     * @date: 2022/5/10 19:15
     */
    @RequestMapping("/list")
    public String queryByConditional(ManagerParam param, Map<String, Object> map,
                                     @Valid PaginationParam paginationParam) {
        setView(map, "list");
        param.buildQueryStr();
        // 设置分页参数
        setPagination(map, paginationParam);
        Result<List<ClientLanguageVo>> listResult = clientConnectionService.queryByConditional(param, paginationParam);
        setResult(map, "listResult", listResult);
        setResult(map, "queryParams", param);
        return view();
    }

    /**
     * @description: 获取所有语言
     * @param: * @param:
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/21 14:09
     */
    @RequestMapping("/getAllLanguage")
    public Result<?> getAllLanguage() throws Exception {
        return clientConnectionService.selectAllLanguage();
    }

    /**
     * @description: 获取所有已扫描的客户端名称
     * @param: * @param:
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/2/21 14:09
     */
    @RequestMapping("/getAllGroupName")
    public Result<?> getAllGroupName() throws Exception {
        return clientConnectionService.selectgetAllGroupName();
    }

    /**
     * @description: 刷新客户端信息
     * @param: * @param: clientType
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/5/5 17:46
     */
    @RequestMapping("/refreshClient")
    public Result<?> refreshClient(String topicName) throws Exception {

        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                clientConnectionService.scanAllClientGroupConnectLanguage(topicName);
            }
        });
        return Result.getOKResult();
    }

    /**
     * @description: 查询单条记录
     * @param: * @param: clientLanguage
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/5/11 20:38
     */
    @RequestMapping("query")
    public Result<?> query(ClientLanguage clientLanguage, boolean withLanguagesList) {
        return clientConnectionService.query(clientLanguage, withLanguagesList);
    }

    /**
     * @description: 手动更新
     * @param: * @param: clientLanguage
     * @return: com.sohu.tv.mq.cloud.util.Result<?>
     * @author fengwang219475
     * @date: 2022/5/10 19:15
     */
    @RequestMapping("update")
    public Result<?> update(ClientLanguage clientLanguage) {
        return clientConnectionService.updateClientData(clientLanguage);
    }

    @Override
    public String viewModule() {
        return "clientConnect";
    }
}
