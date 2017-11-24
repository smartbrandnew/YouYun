package com.broada.carrier.monitor.impl.host.snmp.disk;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.MonitorErrorUtil;
import com.broada.carrier.monitor.impl.host.snmp.disk.SnmpDiskManager.DiskAreaInfo;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.common.util.Unit;
import com.broada.snmputil.SnmpTarget;

/**
 * DISK 使用率 监测参数实体类
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

public class DISKMonitor extends BaseMonitor {
	private static final String ITEMIDX_USEPERCENT = "SNMP-DISK-1";
	private static final String ITEMIDX_FREE_MB = "SNMP-DISK-2";
	private static final String ITEMIDX_TOTAL_MB = "SNMP-DISK-3";

	/**
	 * DISK 监测实现
	 * 
	 * 
	 * @param srv
	 * @return
	 * @throws java.lang.Exception
	 */
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod method = new SnmpMethod(context.getMethod());
		SnmpTarget target = method.getTarget(context.getNode().getIp());
		try {
			SnmpDiskManager snmpDiskManager = new SnmpDiskManager(target);
			List<SnmpDiskManager.DiskAreaInfo> areas = snmpDiskManager.getFixeDiskArea();

			// 因为现在所有分区改为0来表示
			// 为了兼容以前的配置,所以只要实例里有","都表示要监测所有分区
			
			for (DiskAreaInfo match : areas) {		
				MonitorResultRow row = new MonitorResultRow();
				row.setInstCode(match.getInstance());
				row.setInstName(match.getLabel());
				row.setIndicator(ITEMIDX_USEPERCENT, match.getUsage());
				row.setIndicator(ITEMIDX_FREE_MB, Unit.B.to(Unit.MB, match.getFree()));
				row.setIndicator(ITEMIDX_TOTAL_MB, Unit.B.to(Unit.MB, match.getSize()));
				result.addRow(row);
			}
		} catch (Throwable ex) {
			return MonitorErrorUtil.process(ex);
		}
		
		return result;
	}
}
