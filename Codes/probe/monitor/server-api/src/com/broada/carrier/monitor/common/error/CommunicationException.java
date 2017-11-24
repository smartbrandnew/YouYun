package com.broada.carrier.monitor.common.error;


/**
 * 通信异常
 * @author Jiangjw
 */
public class CommunicationException extends BaseException {
	private static final long serialVersionUID = 1L;

	public CommunicationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommunicationException(String message) {
		super(message);
	}

}
