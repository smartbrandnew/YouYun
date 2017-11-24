package com.broada.carrier.monitor.impl.host.cli.usermanager.useraccount;

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

public class CLIUserAccountMonitor extends BaseMonitor {

	private static final String INDEX_PASSWD = "CLI-USERACCOUNT-1";

	private static final String INDEX_UID = "CLI-USERACCOUNT-2";

	private static final String INDEX_GID = "CLI-USERACCOUNT-3";

	private static final String INDEX_COMMENT = "CLI-USERACCOUNT-4";

	private static final String INDEX_HOME = "CLI-USERACCOUNT-5";
	
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
					CLIConstant.COMMAND_USERACCOUNTS);
		} catch (Throwable e) {
			return CLIExecutor.processError(e);
		}
		List users = cliResult.getListTableResult();
		for (int i = 0; i < users.size(); i++) {
			Properties properties = (Properties) users.get(i);
			String name = (String) properties.get("user");
			String passwd = (String) properties.get("passwd");
			String uid = (String) properties.get("uid");
			String gid = (String) properties.get("gid");
			String comment = (String) properties.get("comment");
			String home = (String) properties.get("home");
			MonitorResultRow instance = new MonitorResultRow(name);			
			instance.setIndicator(INDEX_PASSWD, passwd);
			instance.setIndicator(INDEX_UID, uid);
			instance.setIndicator(INDEX_GID, gid);
			instance.setIndicator(INDEX_COMMENT, comment);
			instance.setIndicator(INDEX_HOME, home);
			result.addRow(instance);
		}
		return result;
	}
}
