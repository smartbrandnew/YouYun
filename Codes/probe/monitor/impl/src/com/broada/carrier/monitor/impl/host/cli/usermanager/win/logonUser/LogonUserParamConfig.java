package com.broada.carrier.monitor.impl.host.cli.usermanager.win.logonUser;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;

public class LogonUserParamConfig   extends CLIMultiInstanceConfiger {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doTest() {
		doTest("主机登陆用户信息监测", CLIConstant.COMMAND_WIN_LOGON_USERS);
	}

}