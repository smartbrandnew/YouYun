package com.broada.carrier.monitor.impl.host.cli.cpu;

import java.io.Serializable;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class CLICPUMonitor extends BaseMonitor {

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
		CLIResult cliResult = null;

		float user = 0.0f;
		float sys = 0.0f;
		try {
			long replyTime = System.currentTimeMillis();
			cliResult = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(),
					CLIConstant.COMMAND_CPU);
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			/*
			 * 捕获解析异常
			 */
			try {
				user = Float.parseFloat(cliResult.getPropResult().getProperty("user"));
				sys = Float.parseFloat(cliResult.getPropResult().getProperty("sys"));
			} catch (Throwable e) {
				throw new CLIResultParseException(e);
			}
		} catch (Throwable e) {
			return CLIExecutor.processError(e);
		}
		
		if (user > 100) {
			user = 99;
		}
		if (sys > 100) {
			sys = 99;
		}

		float total = user + sys;
		if (total > 100) {
			total = 100;
		}
		PerfResult[] perfs = new PerfResult[] { new PerfResult("CLI-HOSTCPU-1", total),
				new PerfResult("CLI-HOSTCPU-2", sys),
				new PerfResult("CLI-HOSTCPU-3", user) };
		
		MonitorResult result = new MonitorResult();
		result.setPerfResults(perfs);
		return result;
	}
}
