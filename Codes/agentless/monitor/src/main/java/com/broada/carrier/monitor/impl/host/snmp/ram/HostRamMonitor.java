package com.broada.carrier.monitor.impl.host.snmp.ram;

import java.io.Serializable;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.MonitorErrorUtil;
import com.broada.carrier.monitor.impl.host.snmp.util.Memory;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpProcessManager;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpProcessManagerFactory;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.common.util.Unit;
import com.broada.snmp.SnmpWalk;

/**
 * 主机内存监测监测器实现
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class HostRamMonitor extends BaseMonitor {
	/**
	 * 内存监测实现
	 * @param srv
	 * @return
	 * @throws java.lang.Exception
	 */
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod method = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(method.getTarget(context.getNode().getIp()));
		try {
			SnmpProcessManager mgr = SnmpProcessManagerFactory.getInstance(walk, context.getNode().getId());
			Memory memory = mgr.getMemory();
			result.addPerfResult(new PerfResult("SNMP-HOSTRAM-1", Unit.B.to(Unit.MB, memory.getUsed())));
			result.addPerfResult(new PerfResult("SNMP-HOSTRAM-2", (int) (memory.getUsed() * 100.0 / memory.getSize())));
		} catch (Throwable ex) {
			return MonitorErrorUtil.process(ex);
		} finally {
			walk.close();
		}
		return result;
	}
}