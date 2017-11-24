package com.broada.carrier.monitor.impl.host.cli.netstat;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * 网络端口监测器
 * 
 * @author zhoucy(zhoucy@broada.com.cn) Create By May 5, 2008 9:49:11 AM
 */
public class CLINetStatMonitor extends BaseMonitor {	
	public final static String[] CONDITION_FIELDS = new String[] { "receiveError", "sendError" };

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

	@SuppressWarnings("rawtypes")
	private MonitorResult collect(String taskId, CollectContext context) {
		List netstatList = null;
		try {
			netstatList = CLINetStatExecutor.getNetStatList(taskId, context.getNode(), context.getMethod());
		} catch (Throwable e) {
			return CLIExecutor.processError(e);
		}

		MonitorResult result = new MonitorResult();
		for (int index = 0; index < netstatList.size(); index++) {
			CLINetStatInfo info = (CLINetStatInfo) netstatList.get(index);
			String instKey = info.getName() + info.getNetwork() + info.getAddress();

			MonitorResultRow row = new MonitorResultRow(instKey);
			row.addTag("device:" + info.getName());
			row.setIndicator("CLI-NETSTAT-1", info.getName());
			row.setIndicator("CLI-NETSTAT-2", info.getNetwork());
			row.setIndicator("CLI-NETSTAT-3", info.getAddress());
			row.setIndicator("CLI-NETSTAT-4", info.getMtu());
			if (info.getIpkts() >= 0)
				row.setIndicator("CLI-NETSTAT-5", info.getIpkts());
			if (info.getIerrs() >= 0)
				row.setIndicator("CLI-NETSTAT-6", info.getIerrs());
			if (info.getOpkts() >= 0)
				row.setIndicator("CLI-NETSTAT-7", info.getOpkts());
			if (info.getOerrs() >= 0)
				row.setIndicator("CLI-NETSTAT-8", info.getOerrs());
			if (info.getColl() >= 0)
				row.setIndicator("CLI-NETSTAT-9", info.getColl());
			result.addRow(row);
		}
		return result;
	}
}
