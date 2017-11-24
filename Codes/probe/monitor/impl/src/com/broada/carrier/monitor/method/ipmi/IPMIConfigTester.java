package com.broada.carrier.monitor.method.ipmi;

import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMICollect;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIException;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIParameter;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.ServerType;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.core.IPMICollectImpl;

/**
 * IPMI配置测试类
 * @author Jiangjw
 */
public class IPMIConfigTester {
	/**
	 * 进行一次ipmi访问测试
	 * @param param
	 * @throws IPMIException
	 */
	public void doTest(IPMIParameter param) throws IPMIException {
		IPMICollect collect = new IPMICollectImpl(param);
		ServerType st = collect.checkAccount();
		if (st == ServerType.ERROR)
			throw new IPMIException("IPMI访问失败，请确认访问参数是否正确");
	}
}
