package com.broada.carrier.monitor.impl.host.cli.memory;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLISingleInstanceConfiger;

public class CLIMemoryParamConfiger extends CLISingleInstanceConfiger {

  private static final long serialVersionUID = 3766048946832331545L;

	@Override
	protected void doTest() {
		doTest("主机内存监测",CLIConstant.COMMAND_MEMORY);
	}
}