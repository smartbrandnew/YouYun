package com.broada.carrier.monitor.method.ipmi;

import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIParameter;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.utils.TextUtil;

/**
 * IPMI监测配置参数
 * 
 * @author pippo
 * 
 */
public class IPMIMonitorMethodOption extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolIPMI";

	public IPMIMonitorMethodOption() {
		super();
	}

	public IPMIMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	public String getHost() {
		return (String) getProperties().getByMethod();
	}

	public void setHost(String host) {
		getProperties().setByMethod(host);
	}

	public String getUsername() {
		return getProperties().getByMethod("");
	}

	public void setUsername(String user) {
		getProperties().setByMethod(user);
	}

	public String getPassword() {
		return getProperties().getByMethod("");
	}

	public void setPassword(String password) {
		getProperties().setByMethod(password);
	}

	public String getLevel() {
		return getProperties().get("level", "");
	}

	public void setLevel(String level) {
		getProperties().set("level", level);
	}

	public IPMIParameter toParameter(String host) {
		if (!TextUtil.isEmpty(getHost()))
			host = getHost();		
		return new IPMIParameter(host, getUsername(), getPassword(), getLevel());
	}
}
