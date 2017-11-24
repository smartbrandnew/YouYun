package com.broada.carrier.monitor.impl.host.cli.processstate;

import java.io.Serializable;
import java.util.Map;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class CLIProStateMonitor extends BaseMonitor {
	@Override
	public MonitorResult monitor(MonitorContext context) {
		return collect(context.getTask().getId(), new CollectContext(context));
	}

	/**
	 * 传入参数srvId
	 */
	@Override
	public Serializable collect(CollectContext context) {
		return collect("-1", context);
	}

	private MonitorResult collect(String taskId, CollectContext context) {
		MonitorResult result = new MonitorResult();
		Map processes = null;
		try {
			ProStateExecutor executor = ProStateExecutorFactory.getProcessExecutor(taskId);
			processes = executor.getProcesses(taskId, context.getNode(), context.getMethod());
		} catch (Throwable e) {
			return CLIExecutor.processError(e);
		}
		
		for (Object obj : processes.values()) {
			if (!(obj instanceof CLIProStateMonitorCondition))
				continue;
			CLIProStateMonitorCondition c = (CLIProStateMonitorCondition) obj;

			PerfResult prefCurrentVsize = new PerfResult("CLI-PROCESSSTATE-1", Float.parseFloat(c.getCurrentVsize()));
			prefCurrentVsize.setInstKey(c.getField());
			result.addPerfResult(prefCurrentVsize);

			PerfResult prefCurrentLstart = new PerfResult("CLI-PROCESSSTATE-2", c.getCurrentLstart());
			prefCurrentLstart.setInstKey(c.getField());
			result.addPerfResult(prefCurrentLstart);

			PerfResult prefCurrentEtime = new PerfResult("CLI-PROCESSSTATE-3", c.getCurrentEtime());
			prefCurrentEtime.setInstKey(c.getField());
			result.addPerfResult(prefCurrentEtime);
		}

		return result;
	}
}
