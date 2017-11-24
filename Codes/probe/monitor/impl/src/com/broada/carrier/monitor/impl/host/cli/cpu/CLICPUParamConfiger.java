package com.broada.carrier.monitor.impl.host.cli.cpu;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLISingleInstanceConfiger;

public class CLICPUParamConfiger extends CLISingleInstanceConfiger {

  private static final long serialVersionUID = 3766048946832331545L;

	@Override
	protected void doTest() {
		doTest("主机CPU监测", CLIConstant.COMMAND_CPU);
	}
}
