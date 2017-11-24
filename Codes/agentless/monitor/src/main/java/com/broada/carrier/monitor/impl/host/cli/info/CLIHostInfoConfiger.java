package com.broada.carrier.monitor.impl.host.cli.info;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLISingleInstanceConfiger;

public class CLIHostInfoConfiger extends CLISingleInstanceConfiger {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doTest() {
		doTest("主机基本信息监测", CLIConstant.COMMAND_HOSTINFO);
	}

}
