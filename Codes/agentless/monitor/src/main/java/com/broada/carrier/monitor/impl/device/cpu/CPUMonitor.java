package com.broada.carrier.monitor.impl.device.cpu;

import com.broada.carrier.monitor.method.snmp.collection.ExprObjectMonitor;
import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;

/**
 * CPU使用率监测器实现
 * 
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: NMS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author Maico Pang
 * @version 1.0
 */

public class CPUMonitor extends ExprObjectMonitor {
	private static final String ITEMCODE_USEPERCENT = "CPU-1";
	
	@Override
	protected String getItem() {
		return ITEMCODE_USEPERCENT;
	}

	@Override
	protected PerfType getPerfType() {
		return PerfType.CPU;
	}
}
