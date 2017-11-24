package com.broada.carrier.monitor.common.restful;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.broada.carrier.monitor.common.error.BaseException;
import com.broada.component.utils.error.ErrorUtil;

/**
 * spring mvc控制器基类，用于提供一些控制器基础实现
 */
public class BaseController {
	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

	@Autowired
	protected HttpServletRequest request;

	/**
	 * 拦截异常
	 * @param e
	 * @return
	 */
	@ExceptionHandler(Throwable.class)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleException(Throwable e) {
		return processException(e, request);
	}

	/**
	 * 将一个异常转换成ErrorResponse
	 * @param e
	 * @return
	 */
	public static ErrorResponse processException(Throwable e) {
		return processException(e, null);
	}

	/**
	 * 将一个异常转换成ErrorResponse
	 * @param e
	 * @param request
	 * @return
	 */
	public static ErrorResponse processException(Throwable e, HttpServletRequest request) {
		BaseException err;
		if (("AuthenticationException").equals(e.getClass().getSimpleName().trim())
				|| ("IllegalArgumentException").equals(e.getClass().getSimpleName().trim())) {
			err = new BaseException(e.getClass().getSimpleName(), e.getMessage(), e);
		} else if (e instanceof BaseException || e.getClass().getName().contains("com.broada")) {
			logger.debug("服务调用失败，错误：", e);
			err = (BaseException) e;
		} else if (e instanceof HttpMediaTypeNotSupportedException) {
			err = new BaseException(ErrorUtil.createMessage("服务端出现HTTP Media Type支持错误", e), e);
			logger.debug("服务调用失败，错误：", e);
		} else {
			String message = "服务调用未知失败";
			if (request != null)
				message += "：" + request.getRequestURI();
			logger.warn(message, e);
			String errorMessage;
			if (e.getMessage() != null)
				errorMessage = e.getMessage();
			else
				errorMessage = ErrorUtil.createMessage("服务端出现未知错误", e);
			err = new BaseException(e.getClass().getSimpleName(), errorMessage, e);
		}		
		
		return new ErrorResponse(err.getCode(), err.getMessage(), null, e);
	}

}
