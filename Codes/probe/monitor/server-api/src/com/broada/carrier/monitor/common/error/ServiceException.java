package com.broada.carrier.monitor.common.error;

/**
 * 服务调用时，出现服务内部异常
 * 
 * @author Jiangjw
 */
public class ServiceException extends BaseException {
	private static final long serialVersionUID = 1L;
	private String service;
	private String request;
	private String stack;

	public ServiceException(String code, String message, Throwable cause) {
		super(code, message, cause);
	}

	public ServiceException(String code, String message) {
		super(code, message);
	}

	public ServiceException(String code, String message, String service, String request, String stack) {
		super(code, message);
		this.service = service;
		this.request = request;
		this.stack = stack;
	}

	public String getService() {
		return service;
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(String message) {
		super(message);
	}

	public String getRequest() {
		return request;
	}

	public String getStack() {
		return stack;
	}

}
