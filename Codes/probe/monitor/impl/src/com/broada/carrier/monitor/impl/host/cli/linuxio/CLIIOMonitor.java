package com.broada.carrier.monitor.impl.host.cli.linuxio;

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
		List conditions = null;
		try {
			conditions = CLIIOExecutor.procIO(taskId, context.getNode(), context.getMethod());
		} catch (Throwable fe) {
			return CLIExecutor.processError(fe);
		}
		if (instances == null || instances.length == 0) {
			// 由于CLI IO监测，容易出现没有捕捉到任何磁盘IO信息的情况，导致实例缺失，所以使用此方案，来保障磁盘。
			instances = new MonitorInstance[conditions.size()];
			for (int i = 0; i < conditions.size(); i++) {
				CLIIOMonitorCondition cond = (CLIIOMonitorCondition) conditions.get(i);
				instances[i] = new MonitorInstance(cond.getField());
			}
		}

		MonitorResult result = new MonitorResult();
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
				row.setIndicator("CLI-LINUXDEVICEIO-1", cond.getUtil());
				row.setIndicator("CLI-LINUXDEVICEIO-2", cond.getRrqmPerSecond());
				row.setIndicator("CLI-LINUXDEVICEIO-3", cond.getWrqmPerSecond());
				row.setIndicator("CLI-LINUXDEVICEIO-4", cond.getReadPerSecond());
				row.setIndicator("CLI-LINUXDEVICEIO-5", cond.getWritePerSecond());
				row.setIndicator("CLI-LINUXDEVICEIO-6", cond.getRsecPerSecond());
				row.setIndicator("CLI-LINUXDEVICEIO-7", cond.getWsecPerSecond());
				row.setIndicator("CLI-LINUXDEVICEIO-8", cond.getRkbPerSecond());
				row.setIndicator("CLI-LINUXDEVICEIO-9", cond.getWkbPerSecond());
				row.setIndicator("CLI-LINUXDEVICEIO-10", cond.getAvgrqsz());
				row.setIndicator("CLI-LINUXDEVICEIO-11", cond.getAvgqusz());
				row.setIndicator("CLI-LINUXDEVICEIO-12", cond.getAwait());
				row.setIndicator("CLI-LINUXDEVICEIO-13", cond.getSvctm());
			}
			result.addRow(row);
		}
		return result;
	}
}
