package com.broada.carrier.monitor.impl.device.ifstatus;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.MonitorErrorUtil;
import com.broada.carrier.monitor.impl.common.entity.IfOperState;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.method.snmp.collection.SnmpIntfPerf;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.common.util.Unit;

public class IfstatusMonitor extends BaseMonitor {
	public static final String IFSTATUS_ITEM_INDEX = "IFSTATUS-1";
	public static final String IFSTATUS_ITEM_SPEED = "IFSPEED-1";

	@Override
	public Serializable collect(CollectContext context) {
		return walk(context);
	}

	public static MonitorResult walk(CollectContext context) {
		SnmpMethod method = new SnmpMethod(context.getMethod());
		SnmpIntfPerf perf = new SnmpIntfPerf(method.getTarget(context.getNode().getIp()));
		Vector<Map<String, Object>> v;
		try {
			v = perf.getIfPerf(new String[] { SnmpIntfPerf.IF_INDEX, SnmpIntfPerf.IF_DESCR, SnmpIntfPerf.IF_OPER_STATUS,
					SnmpIntfPerf.IF_SPEED, SnmpIntfPerf.IF_NAME });
		} catch (Throwable e) {
			return MonitorErrorUtil.process(e);
		}

		MonitorResult result = new MonitorResult();
		for (Map<String, Object> ife : v) {
			MonitorResultRow row = new MonitorResultRow();
			row.setInstCode(ife.get(SnmpIntfPerf.IF_INDEX).toString());
			String instName = (String) ife.get(SnmpIntfPerf.IF_NAME);
			if (instName == null || instName.isEmpty())
			  instName = (String) ife.get(SnmpIntfPerf.IF_DESCR);
			row.setInstName(instName);			
			IfOperState operState = IfOperState.checkById(((Number) ife.get(SnmpIntfPerf.IF_OPER_STATUS)).intValue());
			row.setIndicator(IFSTATUS_ITEM_INDEX, operState);
			row.setIndicator(IFSTATUS_ITEM_SPEED, Unit.bps.to(Unit.Mbps, ((Number) ife.get(SnmpIntfPerf.IF_SPEED)).longValue()));
			instName = instName.toLowerCase();
			if (operState != IfOperState.UP
					|| instName.contains("vlan")
					|| instName.contains("null")
					|| instName.contains("loopback"))
				row.setInstMonitor(false);
			result.addRow(row);
		}
		return result;
	}
}