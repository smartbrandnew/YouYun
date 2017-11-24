package com.broada.carrier.monitor.common.error;

/**
 * 警告类异常，一般用于用户界面错误反馈
 * @author Jiangjw
 */
public class WarningException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public WarningException(String message) {
		super(message);
	}
}
