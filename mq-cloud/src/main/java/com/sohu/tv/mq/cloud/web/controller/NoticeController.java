package com.sohu.tv.mq.cloud.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sohu.tv.mq.cloud.bo.Notice;
import com.sohu.tv.mq.cloud.service.NoticeService;
import com.sohu.tv.mq.cloud.util.Result;
/**
 * 通知
 * @Description: 
 * @author yongfeigao
 * @date 2018年7月16日
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {
    
    @Autowired
    private NoticeService noticeService;
    
    /**
     * 获取notice
     * @return
     * @throws Exception
     */
    @RequestMapping
    public Result<Notice> index() throws Exception {
        Result<Notice> noticeResult = noticeService.queryNow();
        return Result.getWebResult(noticeResult);
    }
}
