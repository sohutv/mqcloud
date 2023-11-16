package com.sohu.tv.mq.cloud.web.controller;

import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.Status;
import com.sohu.tv.mq.cloud.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 错误统一处理
 *
 * @Auther: yongfeigao
 * @Date: 2023/10/19
 */
@Controller
@ControllerAdvice
public class MQErrorController implements ErrorController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String ERROR_PATH = "/error";

    @ResponseBody
    @RequestMapping(value = ERROR_PATH)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Result<?> handleError(HttpServletRequest req) {
        logger.warn("404 ip:{} url:{} refer:{}", WebUtil.getIp(req), WebUtil.getUrl(req),
                req.getAttribute("javax.servlet.error.request_uri"));
        return Result.getResult(Status.NOT_FOUND_ERROR);
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Result<Exception> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        logger.error("5xx ip:{} url:{}", WebUtil.getIp(req), WebUtil.getUrl(req), e);
        if (e instanceof BindException) {
            return Result.getWebParamErrorResult(e);
        }
        return Result.getWebErrorResult(e);
    }
}
