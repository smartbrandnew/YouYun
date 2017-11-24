package com.broada.carrier.monitor.method.vmware;

/**
 * vsphere异常
 * 
 * 仅仅只是用来区分异常类型
 * @author Panhk
 * @version 1.0
 * @created 28-九月-2012 10:06:26
 */
public class VSphereException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public VSphereException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public VSphereException(String msg) {
		super(msg);
	}
	
}