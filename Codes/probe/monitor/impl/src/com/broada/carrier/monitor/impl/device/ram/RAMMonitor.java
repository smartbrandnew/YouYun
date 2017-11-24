package com.broada.carrier.monitor.impl.device.ram;

import com.broada.carrier.monitor.method.snmp.collection.ExprObjectMonitor;
import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;

/**
 * RAM使用率监测器实现
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class RAMMonitor  extends ExprObjectMonitor {
	private static final String ITEMIDX_USEPERCENT = "RAM-1";
	
	@Override
	protected String getItem() {
		return ITEMIDX_USEPERCENT;
	}

	@Override
	protected PerfType getPerfType() {
		return PerfType.MEM;
	}
}
