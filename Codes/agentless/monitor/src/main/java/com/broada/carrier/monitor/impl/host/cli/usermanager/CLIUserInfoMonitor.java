package com.broada.carrier.monitor.impl.host.cli.usermanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIDateFormat;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * 类unix系统主机当前登陆用户信息监测实现类
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-6-17 下午04:50:31
 */
public class CLIUserInfoMonitor implements Monitor {

	@Override
	public MonitorResult monitor(MonitorContext context) {
		return collect(context.getTask().getId(), new CollectContext(context));
	}

	@Override
	public Serializable collect(CollectContext context) {
		return collect("-1", context);
	}

	public MonitorResult collect(String taskId, CollectContext context) {
		CLIResult result = null;
		List<Properties> userInfos = new ArrayList<Properties>();
		try {
			result = new CLIExecutor(taskId).execute(context.getNode(),
					context.getMethod(), CLIConstant.COMMAND_CURRENTUSER, 0);
		} catch (Exception e) {
			return CLIExecutor.processError(e);
		}

		userInfos = result.getListTableResult();
		MonitorResult mr = new MonitorResult();
		for (Properties props : userInfos) {
			String tty = props.getProperty("tty");
			String user = props.getProperty("user");
			String loginfrom = props.getProperty("loginfrom");
			String logintime = props.getProperty("logintime");
			if (tty.contains("?") || user.contains("?") || loginfrom.contains("?") || logintime.contains("?"))
				continue;
			MonitorResultRow row = new MonitorResultRow(tty, tty);
			row.setIndicator("CLI-HOSTUSER-1", user);
			row.setIndicator("CLI-HOSTUSER-2", loginfrom);
			row.setIndicator("CLI-HOSTUSER-3", CLIDateFormat.format(logintime));
			mr.addRow(row);
		}

		return mr;
	}

}
