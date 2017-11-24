package com.broada.carrier.monitor.impl.host.cli.processstate;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;

public class CLIProStateParamConfiger extends CLIMultiInstanceConfiger {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doTest() {
  	CLIMonitorMethodOption options;
  	if (getMethod() instanceof CLIMonitorMethodOption)
  		options = (CLIMonitorMethodOption) getMethod();
  	else
  		options = new CLIMonitorMethodOption(getMethod());

  	String sysName = options.getSysname();
  	if(sysName.equalsIgnoreCase("linux")) {
  		doTest("主机进程状态监测", CLIConstant.COMMAND_PROCESSSTATE);
  	} else if(sysName.equalsIgnoreCase("windows")) {
  		doTest("主机进程状态监测", CLIConstant.COMMAND_PROCESSSTATE);
  	} else 
  		doTest("主机进程状态监测", new String[]{CLIConstant.COMMAND_SYSTEMTIME, CLIConstant.COMMAND_PROCESSSTATE});  	
	}

}
