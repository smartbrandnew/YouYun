package com.broada.carrier.monitor.impl.host.cli.usermanager.win.logonUser;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIDateFormat;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class LogonUserMonitor extends BaseMonitor {
	private static final String INDEX_COMPUTER = "WIN-LOGONUSER-1";

	private static final String INDEX_NAME = "WIN-LOGONUSER-2";

	private static final String INDEX_STARTTIME = "WIN-LOGONUSER-3";

	
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
		CLIResult cliResult = null;
		try {
			cliResult = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(), 
					CLIConstant.COMMAND_WIN_LOGON_USERS);
		} catch (Throwable e) {
			return CLIExecutor.processError(e);
		}
		List users = cliResult.getListTableResult();
		for (int i = 0; i < users.size(); i++) {
			Properties properties = (Properties) users.get(i);
			String computer = (String) properties.get("Computer");
			String name = (String) properties.get("Name");
			String startTime = (String) properties.get("StartTime");
			MonitorResultRow instance = new MonitorResultRow(computer + "-" + name);
			instance.setIndicator(INDEX_COMPUTER, computer);
			instance.setIndicator(INDEX_NAME, name);
			instance.setIndicator(INDEX_STARTTIME, CLIDateFormat.format(startTime));
			result.addRow(instance);
		}
		return result;
	}
}
