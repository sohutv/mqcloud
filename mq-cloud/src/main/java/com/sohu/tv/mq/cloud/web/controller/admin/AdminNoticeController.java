package com.sohu.tv.mq.cloud.web.controller.admin;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.bo.Notice;
import com.sohu.tv.mq.cloud.service.NoticeService;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.web.controller.param.NoticeParam;
/**
 * 通知
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月18日
 */
@Controller
@RequestMapping("/admin/notice")
public class AdminNoticeController extends AdminViewController {
    
    @Autowired
    private NoticeService noticeService;
    
    /**
     * 获取notice列表
     * @return
     * @throws Exception
     */
    @RequestMapping("/list")
    public String list(Map<String, Object> map) throws Exception {
        setView(map, "list");
        Result<List<Notice>> noticeListResult = noticeService.queryAll();
        setResult(map, noticeListResult);
        return view();
    }
    
    /**
     * 新增或修改notice
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="/saveOrUpdate", method=RequestMethod.POST)
    public Result<?> saveOrUpdate(@Valid NoticeParam noticeParam) throws Exception {
        Notice notice = new Notice();
        BeanUtils.copyProperties(noticeParam, notice);
        if(notice.getId() <= 0) {
            Result<?> result = noticeService.save(notice);
            return result;
        }
        Result<?> result = noticeService.update(notice);
        return result;
    }
    
    /**
     * 删除notice
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value="/delete/{tid}", method=RequestMethod.POST)
    public Result<?> delete(@PathVariable long tid) throws Exception {
        Result<?> result = noticeService.delete(tid);
        return result;
    }
    
    @Override
    public String viewModule() {
        return "notice";
    }

}
