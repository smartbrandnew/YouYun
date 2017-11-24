package com.broada.carrier.monitor.impl.host.snmp.process;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.MonitorErrorUtil;
import com.broada.carrier.monitor.impl.common.entity.RunState;
import com.broada.carrier.monitor.impl.host.snmp.util.ApplicationProcess;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpProcessManager;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpProcessManagerFactory;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.snmp.SnmpWalk;

/**
 * 进程监测监测器实现
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class ProcessMonitor extends BaseMonitor {
	@Override
	public MonitorResult monitor(MonitorContext context) {
		return collect(context.getTask().getId(), new CollectContext(context));
	}	
	
	@Override
	public Serializable collect(CollectContext context) {
		return collect("-1", context);
	}
  
	private MonitorResult collect(String taskId, CollectContext context) {	
  	SnmpMethod method = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(method.getTarget(context.getNode().getIp()));
		try {			
			SnmpProcessManager mgr = SnmpProcessManagerFactory.getInstance(walk, context.getNode().getId());
			List ps = null;
			ps = mgr.getApliedProcess();
			MonitorResult mr = new MonitorResult();
			
			if (context.getInstances() != null) {
				for (MonitorInstance inst : context.getInstances()) {
					MonitorResultRow row = new MonitorResultRow(inst);
					row.setIndicator("SNMP-PROCESS-4", RunState.STOP);
					mr.addRow(row);
				}
			}
			
			for (Object object : ps) {
				ApplicationProcess process = (ApplicationProcess) object;				
				MonitorResultRow row = mr.getRow(process.getCode()); 
				if (row == null) {
					row = new MonitorResultRow();
					row.setInstCode(process.getCode());
					row.setInstName(process.getName());
					mr.addRow(row);
				}
				row.setIndicator("SNMP-PROCESS-1", process.getProcMem());
				row.setIndicator("SNMP-PROCESS-2", process.getProcMemPercent());
				row.setIndicator("SNMP-PROCESS-3", process.getProcCPUPercent());
				row.setIndicator("SNMP-PROCESS-4", RunState.RUNNING);				
			}
			return mr;
		} catch (Throwable e) {
			return MonitorErrorUtil.process(e);
		} finally {
			walk.close();
		}
	}
}