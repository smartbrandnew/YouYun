package com.broada.carrier.monitor.common.restful;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.broada.carrier.monitor.common.error.BaseException;
import com.broada.carrier.monitor.common.error.ServiceException;

/**
 * RESTful API调用异常
 */
public class ErrorResponse {
	private String errCode;
	private String message;
	private String request;
	private String stack;

	public ErrorResponse() {
	}

	public ErrorResponse(BaseException cause) {
		this(cause.getCode(), cause.getMessage(), null, cause);
		if (cause instanceof ServiceException) {
			ServiceException se = (ServiceException) cause;
			setRequest(se.getRequest());
			setStack("调用[" + se.getService() + "]时异常：\n" + se.getStack());
		} else
			setStack(createStack(cause));
	}

	public ErrorResponse(String errCode, String message, String request, Throwable cause) {
		this.errCode = errCode;
		this.message = message;
		this.request = request;
		if (cause instanceof ServiceException) {
			ServiceException se = (ServiceException) cause;
			setStack("调用[" + se.getService() + "]时异常：\n" + se.getStack());
		} else
			setStack(createStack(cause));
	}

	private static String createStack(Throwable cause) {
		if (cause == null)
			return null;

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		cause.printStackTrace(ps);
		return os.toString();
	}

	public String getRequest() {
		return request;
	}

	public String getErrCode() {
		return errCode;
	}

	public String getMessage() {
		return message;
	}

	public String getStack() {
		return stack;
	}

	public void setStack(String stack) {
		this.stack = stack;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setRequest(String request) {
		this.request = request;
	}
}
