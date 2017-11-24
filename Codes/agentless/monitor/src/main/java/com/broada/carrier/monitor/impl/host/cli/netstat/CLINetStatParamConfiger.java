package com.broada.carrier.monitor.impl.host.cli.netstat;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIMultiInstanceConfiger;

/**
 * 网络端口监测配置
 * 
 * @author zhoucy(zhoucy@broada.com.cn)
 * Create By May 5, 2008 9:48:00 AM
 */
public class CLINetStatParamConfiger extends CLIMultiInstanceConfiger {
	
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -5049168848057109724L;
  
	@Override
	protected void doTest() {
		doTest("主机内存监测", CLIConstant.COMMAND_NETSTAT);
	}
}