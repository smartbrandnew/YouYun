package com.broada.carrier.monitor.impl.virtual.hypervisor.info;

import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;

public class CLIHyperVInfoConfiger extends CLIMultiInstanceConfiger {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doTest() {
		doTest("Hyper-V监测", "hyperV");
	}

}
