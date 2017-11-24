package com.broada.carrier.monitor.impl.host.cli.usermanager;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;

/**
 * 类unix系统主机当前登陆用户信息监测配置器实现
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-6-17 下午05:08:31
 */
public class CLIUserInfoParamConfiger extends CLIMultiInstanceConfiger {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doTest() {
		doTest("主机当前登陆用户监测", CLIConstant.COMMAND_CURRENTUSER);
	}

}