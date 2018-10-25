package com.sohu.tv.mq.cloud.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.util.WebUtil;
/**
 * 404处理器
 * 
 * @author yongfeigao
 * @date 2018年9月12日
 */
@RestController
public class NotFoundController implements org.springframework.boot.autoconfigure.web.ErrorController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String ERROR_PATH = "/error";

    @RequestMapping(value = ERROR_PATH)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Result<?> handleError(HttpServletRequest req) {
        logger.warn("404 ip:{} url:{} refer:{}", WebUtil.getIp(req), WebUtil.getUrl(req), 
                req.getAttribute("javax.servlet.error.request_uri"));
        return Result.getResult(Status.NOT_FOUND_ERROR);
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}
