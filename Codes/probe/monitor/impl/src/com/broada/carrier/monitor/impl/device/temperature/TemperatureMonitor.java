package com.broada.carrier.monitor.impl.device.temperature;

import com.broada.carrier.monitor.method.snmp.collection.ExprObjectMonitor;
import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;

/**
 * <p>
 * Title: 网络设备温度监测器
 * </p>
 * <p>
 * Description: 产品部
 * </p>
 * <p>
 * Copyright: Copyright (c) 2010
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author 蔡 康
 * @version 3.3.0
 */
public class TemperatureMonitor extends ExprObjectMonitor {
	private static final String ITEMIDX_USEPERCENT = "TEMPERATURE-1";
	
	@Override
	protected String getItem() {
		return ITEMIDX_USEPERCENT;
	}

	@Override
	protected PerfType getPerfType() {
		return PerfType.TEMP;
	}
}
