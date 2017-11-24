package com.broada.carrier.monitor.method.apache;

import com.broada.carrier.monitor.method.common.HttpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class ApacheMethodOption extends HttpMethod {

	private static final long serialVersionUID = 1L;

	public static final String TYPE_ID = "ProtocolApache";

	public ApacheMethodOption() {
		super();
	}

	public ApacheMethodOption(MonitorMethod copy) {
		super(copy);
	}

	public void setDomain(String domain) {
		getProperties().put("domain", domain);
	}

	public String getDomain() {
		return getProperties().get("domain", "");
	}

}
