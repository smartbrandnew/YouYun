package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

/**
 * 进行采集时发生的异常
 * 
 * @author pippo Create By 2014-5-16 上午02:38:23
 */
public class IPMIException extends Exception {

	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 2438679822684435831L;

	/**
   * 
   */
	public IPMIException() {
	}

	/**
	 * @param message
	 */
	public IPMIException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IPMIException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IPMIException(String message, Throwable cause) {
		super(message, cause);
	}

}
