package com.broada.carrier.monitor.impl.virtual.hypervisor.cpu;

import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;

public class CLIHyperVCPUConfiger extends CLIMultiInstanceConfiger {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doTest() {
		doTest("Hyper-V CPU监测", "hyperVCPU");
	}

}
