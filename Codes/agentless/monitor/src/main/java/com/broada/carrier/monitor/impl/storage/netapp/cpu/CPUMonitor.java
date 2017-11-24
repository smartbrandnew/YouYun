package com.broada.carrier.monitor.impl.storage.netapp.cpu;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.host.snmp.disk.SnmpDiskManager.DiskAreaInfo;
import com.broada.carrier.monitor.impl.host.snmp.util.NetAppSnmpProcessManager;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpErrorUtil;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.error.MonitorException;
import com.broada.common.util.Unit;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;

/**
 * NETAPP CPU使用率监测器实现
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
 * @author Shoulw
 * @version 1.0
 */

public class CPUMonitor extends BaseMonitor {
	
	private static final String ITEMCODE_USEPERCENT = "NETAPP-CPU-1";
	private static final String ITEMCODE_INTERRUP = "NETAPP-CPU-2";
	private static final String ITEMCODE_IDLETIME = "NETAPP-CPU-3";
	private static final String ITEMCODE_CPUUPTIME = "NETAPP-CPU-4";
	/**
	 * 需要提供snmpwalk的初始化数据
	 */
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		List<CPU> ps = null;
		try {
			NetAppSnmpProcessManager mgr = new NetAppSnmpProcessManager(walk);
			try {
				ps = mgr.getApliedCpu();
			} catch (Exception e) {
				return null;
			}
			 
			 for (CPU cpu : ps) {		
					MonitorResultRow row = new MonitorResultRow();
					row.setInstCode(cpu.getInstance());
					row.setInstName(cpu.getLabel());
					row.setIndicator(ITEMCODE_USEPERCENT, cpu.getUseRateValue());
					row.setIndicator(ITEMCODE_INTERRUP, cpu.getInterruptRateValue());
					row.setIndicator(ITEMCODE_IDLETIME, cpu.getVacancyRateValue());
					row.setIndicator(ITEMCODE_CPUUPTIME, cpu.getCpuuptime());
					result.addRow(row);
				}
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			walk.close();
		}
		return result;
	}
}
