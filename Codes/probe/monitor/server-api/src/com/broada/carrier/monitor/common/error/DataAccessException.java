package com.broada.carrier.monitor.common.error;


/**
 * 数据访问相关异常
 * @author Jiangjw
 */
public class DataAccessException extends BaseException {
	private static final long serialVersionUID = 1L;

	public DataAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataAccessException(String message) {
		super(message);
	}

}
