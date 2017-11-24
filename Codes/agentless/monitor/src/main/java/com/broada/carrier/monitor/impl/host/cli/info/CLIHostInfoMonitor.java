package com.broada.carrier.monitor.impl.host.cli.info;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class CLIHostInfoMonitor extends BaseMonitor {
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
		List<CLIHostInfoMonitorCondition> hostInfoList;
		hostInfoList = CLIHostInfoExecutor.getHostInfoConditions(taskId, context, 0);
		MonitorResult result = new MonitorResult();
		for (Object object : hostInfoList) {			
			CLIHostInfoMonitorCondition condition = (CLIHostInfoMonitorCondition) object;
			result.addPerfResult(new PerfResult(condition.getField(), condition.getValue()));
		}		
		return result;
	}


}
