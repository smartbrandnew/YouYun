package com.broada.carrier.monitor.impl.host.cli.io;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class CLIIOMonitor extends BaseMonitor {

	@Override
	public MonitorResult monitor(MonitorContext context) {
		return collect(context.getTask().getId(), new CollectContext(context), context.getInstances());
	}

	/**
	 * 传入参数srvId
	 */
	@Override
	public Serializable collect(CollectContext context) {
		return collect("-1", context, null);
	}

	private MonitorResult collect(String taskId, CollectContext context, MonitorInstance[] instances) {
		MonitorResult result = new MonitorResult();

		List conditions = null;
		try {
			conditions = CLIIOExecutor.procIO(taskId, context.getNode(), context.getMethod());
		} catch (Throwable e) {
			return CLIExecutor.processError(e);
		}

		if (instances == null || instances.length == 0) {
			// 由于CLI IO监测，容易出现没有捕捉到任何磁盘IO信息的情况，导致实例缺失，所以使用此方案，来保障磁盘。
			instances = new MonitorInstance[conditions.size()];
			for (int i = 0; i < conditions.size(); i++) {
				CLIIOMonitorCondition cond = (CLIIOMonitorCondition) conditions.get(i);				
				instances[i] = new MonitorInstance(cond.getField());				
			}
		}
		
		for (MonitorInstance inst : instances) {
			MonitorResultRow row = new MonitorResultRow(inst);
			CLIIOMonitorCondition cond = null;
			for (int index = 0; index < conditions.size(); index++) {
				CLIIOMonitorCondition temp = (CLIIOMonitorCondition) conditions.get(index);
				if (temp.getField().equals(row.getInstCode())) {
					cond = temp;
					break;
				}
			}
			if (cond != null) {				
				row.setIndicator("CLI-DEVICEIO-1", cond.getBusy());
				row.setIndicator("CLI-DEVICEIO-2", cond.getAvque());
				row.setIndicator("CLI-DEVICEIO-3", cond.getRwPerSecond());
				row.setIndicator("CLI-DEVICEIO-4", cond.getBlksPerSecond());
				row.setIndicator("CLI-DEVICEIO-5", cond.getAvwait());
				row.setIndicator("CLI-DEVICEIO-6", cond.getAvserv());
			}
			result.addRow(row);
		}

		return result;
	}
}
