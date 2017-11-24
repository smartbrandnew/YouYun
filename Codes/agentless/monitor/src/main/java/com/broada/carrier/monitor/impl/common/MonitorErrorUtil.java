package com.broada.carrier.monitor.impl.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.snmp.SnmpException;

public class MonitorErrorUtil {
	private static final Logger logger = LoggerFactory.getLogger(MonitorErrorUtil.class);
	
	public static MonitorResult process(Throwable e) {
		ErrorUtil.warn(logger, "监测失败", e);		
		MonitorResult result = new MonitorResult();
		result.setState(MonitorState.FAILED);
		if (e instanceof SnmpException)
			result.setMessage(e.getMessage());
		else
			result.setMessage(ErrorUtil.createMessage("监测失败", e));
		return result;
	}
}
