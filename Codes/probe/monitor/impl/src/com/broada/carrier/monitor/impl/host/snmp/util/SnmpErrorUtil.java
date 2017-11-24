package com.broada.carrier.monitor.impl.host.snmp.util;

import com.broada.carrier.monitor.spi.error.MonitorException;
import com.broada.snmputil.SnmpException;

public class SnmpErrorUtil {

	public static MonitorException createError(SnmpException e) {
		return new MonitorException(e.getMessage(), e);
	}

}
