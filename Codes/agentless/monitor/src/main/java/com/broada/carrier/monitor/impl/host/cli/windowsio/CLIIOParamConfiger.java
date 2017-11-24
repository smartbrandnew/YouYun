package com.broada.carrier.monitor.impl.host.cli.windowsio;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;

/**
 * windows磁盘I/O监测参数配置器
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-6-30 下午05:18:52
 */
public class CLIIOParamConfiger extends CLIMultiInstanceConfiger {

  private static final long serialVersionUID = 3766048946832331545L;

	@Override
	protected void doTest() {
		doTest("设备IO", CLIConstant.COMMAND_IO);
	}
}
