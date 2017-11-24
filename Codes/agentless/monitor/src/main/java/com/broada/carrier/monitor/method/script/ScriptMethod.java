package com.broada.carrier.monitor.method.script;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class ScriptMethod extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolScript";
	
	public ScriptExecuteSide getExecuteSide() {
		return ScriptExecuteSide.valueOf(getProperties().getByMethod(ScriptExecuteSide.PROBE.name()));
	}

	public void setExecuteSide(ScriptExecuteSide executeSide) {
		getProperties().setByMethod(executeSide.name());
	}

	public int getAgentPort() {
		return getProperties().getByMethod(1850);
	}

	public void setAgentPort(int agentPort) {
		getProperties().setByMethod(agentPort);
	}

	public ScriptMethod() {
	}

	public ScriptMethod(MonitorMethod copy) {
		super(copy);
	}

	public void verify() {
		if (getExecuteSide() == ScriptExecuteSide.AGENT && getAgentPort() < 0)
			throw new IllegalArgumentException("代理端口必须大于0");
	}
}
