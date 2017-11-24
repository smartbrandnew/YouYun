package com.broada.carrier.monitor.common.error;

/**
 * 异常基类
 * @author Jiangjw
 */
public class BaseException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private String code;
	
	/**
	 * 使用指定的错误编码与错误消息构造异常
	 * @param code
	 * @param message
	 */
	public BaseException(String code, String message) {
		super(message);
		this.code = code;
	}
	
	/**
	 * 使用指定的错误编码、错误消息与异常构造异常
	 * @param code
	 * @param message
	 * @param cause
	 */
	public BaseException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	/**
	 * 使用指定错误消息与异常构造异常
	 * @param message
	 * @param cause
	 */
	public BaseException(String message, Throwable cause) {
		this(null, message, cause);
	}

	/**
	 * 使用指定错误消息构造异常
	 * @param message
	 */
	public BaseException(String message) {
		this(null, message);
	}

	/**
	 * 异常编码，如果没有指定异常编码，将使用异常类名作为异常编码
	 * @return
	 */
	public String getCode() {
		if (code == null)
			code = getClass().getSimpleName();
		return code;
	}
}
