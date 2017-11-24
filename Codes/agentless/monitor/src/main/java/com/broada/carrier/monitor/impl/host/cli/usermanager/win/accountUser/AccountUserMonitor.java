package com.broada.carrier.monitor.impl.host.cli.usermanager.win.accountUser;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class AccountUserMonitor extends BaseMonitor {

	private static final String INDEX_NAME = "WIN-ACCOUNTUSER-1";

	private static final String INDEX_FULLNAME = "WIN-ACCOUNTUSER-2";

	private static final String INDEX_DOMAIN = "WIN-ACCOUNTUSER-3";

	private static final String INDEX_DESCRIPTION = "WIN-ACCOUNTUSER-4";
	
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
					CLIConstant.COMMAND_WIN_ACCOUNT_USERS);
		} catch (Throwable e) {
			return CLIExecutor.processError(e);
		}
		List users = cliResult.getListTableResult();
		for (int i = 0; i < users.size(); i++) {
			Properties properties = (Properties) users.get(i);
			String name = (String) properties.get("Name");
			String fullName = (String) properties.get("FullName");
			String domain = (String) properties.get("Domain");
			String description = (String) properties.get("Description");
			MonitorResultRow instance = new MonitorResultRow(domain + "-" + name);			
			instance.setIndicator(INDEX_NAME, name);
			instance.setIndicator(INDEX_FULLNAME, fullName);
			instance.setIndicator(INDEX_DOMAIN, domain);
			instance.setIndicator(INDEX_DESCRIPTION, description);
			result.addRow(instance);
		}
		return result;
	}
}
