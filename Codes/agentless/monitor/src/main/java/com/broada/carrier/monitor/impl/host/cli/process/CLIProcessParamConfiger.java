package com.broada.carrier.monitor.impl.host.cli.process;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;

public class CLIProcessParamConfiger extends CLIMultiInstanceConfiger {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doTest() {
		doTest("主机进程监测", new String[]{CLIConstant.COMMAND_PROCESS, CLIConstant.COMMAND_TOTALMEMORY});
	}

}