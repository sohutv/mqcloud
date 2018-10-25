package com.sohu.tv.mq.cloud.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.cloud.util.WebUtil;

/**
 * 错误统一处理
 * 
 * @author yongfeigao
 * @date 2017年10月9日 下午2:29:06
 */
@ControllerAdvice
public class ErrorController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public Result<Exception> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
	    logger.error("5xx ip:{} url:{}", WebUtil.getIp(req), WebUtil.getUrl(req), e);
		if (e instanceof BindException) {
		    return Result.getWebParamErrorResult(e);
		}
		return Result.getWebErrorResult(e);
	}
}
