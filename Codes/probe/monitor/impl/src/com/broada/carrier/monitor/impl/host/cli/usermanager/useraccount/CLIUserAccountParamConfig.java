package com.broada.carrier.monitor.impl.host.cli.usermanager.useraccount;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;

/**
 * unix/linux用户注册帐户信息配置器类
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-7-24 下午04:11:52
 */
public class CLIUserAccountParamConfig extends CLIMultiInstanceConfiger {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doTest() {
		doTest("主机注册帐户信息监测", CLIConstant.COMMAND_USERACCOUNTS);
	}

}
