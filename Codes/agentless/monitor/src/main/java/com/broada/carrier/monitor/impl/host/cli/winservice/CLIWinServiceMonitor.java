package com.broada.carrier.monitor.impl.host.cli.winservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.RunState;
import com.broada.carrier.monitor.impl.host.snmp.winservice.WinService;
import com.broada.carrier.monitor.impl.host.snmp.winservice.WinServiceMonitor;
import com.broada.carrier.monitor.impl.host.snmp.winservice.WinServiceState;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class CLIWinServiceMonitor extends BaseMonitor {
	@Override
	public MonitorResult monitor(MonitorContext context) {
		return collect(context.getTask().getId(), new CollectContext(context));
	}	
	
	@Override
	public Serializable collect(CollectContext context) {
		return collect("-1", context);
	}

	private MonitorResult collect(String taskId, CollectContext context) {
		List<WinService> conditions = null;
		try {
			conditions = collect(taskId, context.getNode(), context.getMethod());
		} catch (Throwable fe) {
			return CLIExecutor.processError(fe);
		}
		
		MonitorResult result = new MonitorResult();
		if (context.getInstances() != null) {
			for (MonitorInstance inst : context.getInstances()) {
				MonitorResultRow row = new MonitorResultRow(inst);
		    row.setIndicator(WinServiceMonitor.IDX_SUNSTATE, RunState.STOP);
	      result.addRow(row);
			}
		}
		
		for (WinService item : conditions) {
			MonitorResultRow row = result.getRow(item.getWinServiceKey());
			if (row == null) {
				row = new MonitorResultRow(item.getWinServiceKey(), item.getWinServiceName());
				result.addRow(row);
			}
			row.setIndicator(WinServiceMonitor.IDX_SUNSTATE, item.getWinServiceState().getRunState());			
		}
		return result;
	}

	private List<WinService> collect(String taskId, MonitorNode node, MonitorMethod method) {
		CLIResult result = null;
		result = new CLIExecutor(taskId).execute(node, method, "service");

		List ioInfos = result.getListTableResult();
		List ioConds = new ArrayList();
		for (int index = 0; index < ioInfos.size(); index++) {
			Properties properties = (Properties) ioInfos.get(index);
			String name = properties.get("caption").toString().trim();
			String state = (String) properties.get("state");

			WinService cond = new WinService();
			cond.setWinServiceKey(name);
			cond.setWinServiceName(name);
			cond.setWinServiceState(getWinState(state));
			ioConds.add(cond);
		}
		return ioConds;
	}

	private static WinServiceState getWinState(String state) {
		if ("Running".equalsIgnoreCase(state))
			return WinServiceState.ACTIVE;
		else
			return WinServiceState.STOP;
	}
}
