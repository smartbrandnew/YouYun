package com.broada.carrier.monitor.spi.error;

import com.broada.carrier.monitor.common.error.BaseException;

public class MonitorException extends BaseException {
	private static final long serialVersionUID = 1L;

	public MonitorException(String message, Throwable cause) {
		super(message, cause);
	}

	public MonitorException(String message) {
		super(message);
	}

}
