package com.broada.carrier.monitor.impl.host.cli.io;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;

public class CLIIOParamConfiger extends CLIMultiInstanceConfiger {

  private static final long serialVersionUID = 3766048946832331545L;

	@Override
	protected void doTest() {
		doTest("设备IO", CLIConstant.COMMAND_IO);
	}
}
