package com.broada.carrier.monitor.server.api.error;

import com.broada.carrier.monitor.common.error.BaseException;

public class TargetNotExistsException extends BaseException {
	private static final long serialVersionUID = 1L;

	public TargetNotExistsException(String targetId) {
		super(String.format("监测项不存在：%s", targetId));
	}
	
	public TargetNotExistsException(String targetType, String targetId) {
		super(String.format("%s不存在：%s", targetType, targetId));
	}

}
