package com.broada.carrier.monitor.method.script;

import com.broada.agent.config.HostAgentClient;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.numen.agent.original.service.OriginalAgent;

public class ScriptTest {
	public void test(String host, ScriptMethod method) {
		try {
			OriginalAgent agent = HostAgentClient.getHostAgent(host, method.getAgentPort());
			agent.ping();
		} catch (Throwable e) {
			throw ErrorUtil.createIllegalArgumentException("无法连接Agent", e);
		}
	}
}
