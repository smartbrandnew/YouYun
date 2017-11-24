package com.broada.carrier.monitor.impl.host.cli.process;

import java.io.Serializable;
import java.util.Map;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.RunState;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.utils.StringUtil;

public class CLIProcessMonitor extends BaseMonitor {
	@Override
	public MonitorResult monitor(MonitorContext context) {
		return collect(context.getTask().getId(), new CollectContext(context));
	}

	@Override
	public Serializable collect(CollectContext context) {
		return collect("-1", context);
	}

	private MonitorResult collect(String taskId, CollectContext context) {
		Map<String, Object> processes = null;
		try {
			ProcessExecutor executor = ProcessExecutorFactory.getProcessExecutor(taskId);
			processes = executor.getProcesses(taskId, context.getNode(), context.getMethod());
		} catch (Throwable e) {
			return CLIExecutor.processError(e);
		}

		MonitorResult result = new MonitorResult();
		if (context.getInstances() != null) {
			for (MonitorInstance inst : context.getInstances()) {
				MonitorResultRow row = new MonitorResultRow(inst);
				row.setIndicator("CLI-PROCESS-4", RunState.STOP);
				result.addRow(row);
			}
		}

		for (Object obj : processes.values()) {
			if (!(obj instanceof CLIProcessMonitorCondition))
				continue;
			CLIProcessMonitorCondition c = (CLIProcessMonitorCondition) obj;
			String instCode = c.getField();
			if (instCode.contains("/"))
				instCode = instCode.substring(instCode.lastIndexOf("/") + 1);

			MonitorResultRow row = result.getRow(instCode);
			if (row == null) {
				row = new MonitorResultRow(instCode);
				result.addRow(row);
			}
			if (!StringUtil.isNullOrBlank(c.getCurrmemory())) {
				row.setIndicator("CLI-PROCESS-1", Float.parseFloat(c.getCurrmemory()));
			} else {
				row.setIndicator("CLI-PROCESS-1", Float.parseFloat("0.0"));
			}
			if (!StringUtil.isNullOrBlank(c.getCurrcpu())) {
				row.setIndicator("CLI-PROCESS-2", Float.parseFloat(c.getCurrcpu()));
			} else {
				row.setIndicator("CLI-PROCESS-2", Float.parseFloat("0.0"));
			}
			if (!StringUtil.isNullOrBlank(c.getCurrMemoryUtil())) {
				row.setIndicator("CLI-PROCESS-3", Float.parseFloat(c.getCurrMemoryUtil()));
			} else {
				row.setIndicator("CLI-PROCESS-3", Float.parseFloat("0.0"));
			}
			row.setIndicator("CLI-PROCESS-4", RunState.RUNNING);
		}
		return result;
	}
}
