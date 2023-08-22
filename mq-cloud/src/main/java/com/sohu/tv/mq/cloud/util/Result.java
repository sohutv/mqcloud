package com.sohu.tv.mq.cloud.util;

import com.sohu.tv.mq.cloud.web.controller.param.PaginationParam;
import com.sohu.tv.mq.util.JSONUtil;

import java.util.Collection;
import java.util.Map;

/**
 * web请求结果
 * 
 * @author yongfeigao
 * @date 2017年10月9日 上午11:39:21
 */
@SuppressWarnings("unchecked")
public class Result<T> {
    public static final String RESPONSE = "response";
    public static final String PAGINATION = "pagination";
    public static final String VIEW = "view";
	// 请求状态
	private int status;
	// 提示信息
	private String message;
	// 真正的结果
	private T result;
    // 异常
    private Exception exception;
    
    /**
     * 获取OK结果
     * 
     * @return
     */
    public static <T> Result<T> getOKResult() {
        return getResult(Status.OK);
    }

	/**
	 * 根据@Status 获取结果
	 * 
	 * @param status
	 * @return
	 */
	public static <T> Result<T> getResult(Status status) {
		return new Result<T>()
				.setStatus(status.getKey())
				.setMessage(status.getValue());
	}

    /**
     * 获取json格式数据
     * 
     * @param status
     * @return
     */
    public static String getJsonResult(Status status) {
        return getJsonResult(getResult(status));
    }
    
    /**
     * 获取json格式数据
     * 
     * @return
     */
    public static String getJsonResult(Result<?> result) {
        return JSONUtil.toJSONString(result);
    }

	/**
	 * 获取正常返回结果
	 * 
	 * @return
	 */
    public static <T> Result<T> getResult(Object result) {
		if (result == null) {
			return getResult(Status.NO_RESULT);
		}
		return (Result<T>) getResult(Status.OK).setResult(result);
	}

    /**
     * 保存结果
     * 
     * @return
     */
    public static void setResult(Map<String, Object> map, Object result) {
        if (result == null) {
            map.put(RESPONSE, getResult(Status.NO_RESULT));
            return;
        }
        map.put(RESPONSE, getResult(Status.OK).setResult(result));
    }
    
    /**
     * 保存结果
     * 
     * @return
     */
    public static void setResult(Map<String, Object> map, Result<?> result) {
        if (result == null) {
            map.put(RESPONSE, getResult(Status.NO_RESULT));
            return;
        }
        map.put(RESPONSE, result);
    }
    
    /**
     * 保存分页结果
     * 
     * @return
     */
    public static void setResult(Map<String, Object> map, PaginationParam paginationParam) {
        if (paginationParam == null) {
            map.put(PAGINATION, getResult(Status.NO_RESULT));
            return;
        }
        map.put(PAGINATION, getResult(Status.OK).setResult(paginationParam));
    }

    /**
     * 保存视图变量
     * 
     * @return
     */
    public static void setView(Map<String, Object> map, Object result) {
        map.put(VIEW, result);
    }
	
	/**
	 * 获取异常返回结果
	 * 
	 * @return
	 */
	public static <T> Result<T> getWebErrorResult(Exception e) {
		return getErrorResult(Status.WEB_ERROR, e);
	}
	
	/**
     * 获取异常返回结果
     * 
     * @return
     */
    public static <T> Result<T> getWebParamErrorResult(Exception e) {
        return getErrorResult(Status.PARAM_ERROR, e);
    }
    
    /**
     * 获取异常返回结果
     * 
     * @param status
     * @return
     */
    public static <T> Result<T> getErrorResult(Status status, Exception e) {
        String error = e.getMessage();
        if(error == null) {
            error = e.getClass().getName();
        }
        return (Result<T>) getResult(status).setMessage(error);
    }

    /**
     * 获取请求异常返回结果
     *
     * @return
     */
    public static <T> Result<T> getRequestErrorResult(Exception e) {
        return (Result<T>) getResult(Status.REQUEST_ERROR).setException(e);
    }

	/**
     * 获取Db异常返回结果
     * 
     * @return
     */
    public static <T> Result<T> getDBErrorResult(Exception e) {
        return (Result<T>) getResult(Status.DB_ERROR).setException(e);
    }
    
    /**
     * 处理结果
     * 
     * @return
     */
    public static <T> Result<T> getWebResult(Result<T> result) {
        if(result.isOK()) {
            return result;
        }
        if(result.getException() != null) {
            return Result.getWebErrorResult(result.getException());
        }
        return result;
    }
    
    /**
     * 外部可以使用此方法便捷的判断结果是否正确
     * @return
     */
    public boolean isOK() {
        return Status.OK.getKey() == status;
    }
    
    /**
     * 外部可以使用此方法便捷的判断结果是否正确
     * @return
     */
    public boolean isNotOK() {
        return !isOK();
    }

    /**
     * 如果结果是Collection，判断是否有数据
     * 
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean isNotEmpty() {
        if(isNotOK()) {
            return false;
        }
        if(result instanceof Collection && ((Collection) result).size() > 0){
            return true;
        }
        if(result instanceof Map && ((Map) result).size() > 0){
            return true;
        }
        return false;
    }

    /**
     * 如果结果是Collection，判断是否有数据
     * 
     * @return
     */
    public boolean isEmpty() {
        return !isNotEmpty();
    }

	public int getStatus() {
		return status;
	}

	public Result<T> setStatus(int status) {
		this.status = status;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public Result<T> setMessage(String message) {
		this.message = message;
		return this;
	}

    public Result<T> formatMessage(Object... args) {
        this.message = String.format(this.message, args);
        return this;
    }

	public T getResult() {
		return result;
	}

	public Result<T> setResult(T result) {
		this.result = result;
		return this;
	}

    public Exception getException() {
        return exception;
    }

    public Result<T> setException(Exception exception) {
        this.exception = exception;
        return this;
    }
    
    /**
     * 转换为json格式数据
     * 
     * @return
     */
    public String toJson() {
        return JSONUtil.toJSONString(this);
    }

    @Override
    public String toString() {
        return "Result [status=" + status + ", message=" + message + ", result=" + result + ", exception=" + exception
                + ", isOK()=" + isOK() + "]";
    }
}
