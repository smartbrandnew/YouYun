package com.broada.carrier.monitor.impl.host.cli.disk;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;

public class CLIDiskParamConfiger extends CLIMultiInstanceConfiger {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -2636945019775050064L;

	@Override
	protected void doTest() {
		doTest("磁盘监测", CLIConstant.COMMAND_DISK);
	}
}
