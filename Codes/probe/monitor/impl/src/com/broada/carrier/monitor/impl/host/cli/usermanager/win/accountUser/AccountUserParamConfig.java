package com.broada.carrier.monitor.impl.host.cli.usermanager.win.accountUser;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;

public class AccountUserParamConfig  extends CLIMultiInstanceConfiger {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doTest() {
		doTest("主机注册帐户信息监测", CLIConstant.COMMAND_WIN_ACCOUNT_USERS);
	}

}
