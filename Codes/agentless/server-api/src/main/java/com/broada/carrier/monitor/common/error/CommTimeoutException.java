package com.broada.carrier.monitor.common.error;


/**
 * 通信超市异常
 * @author Jiangjw
 */
public class CommTimeoutException extends CommunicationException {
	private static final long serialVersionUID = 1L;
	
	public CommTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommTimeoutException(String message) {
		super(message);
	}

}
