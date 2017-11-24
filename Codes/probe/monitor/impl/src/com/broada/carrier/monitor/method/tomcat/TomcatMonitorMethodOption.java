package com.broada.carrier.monitor.method.tomcat;

import com.broada.carrier.monitor.method.common.HttpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class TomcatMonitorMethodOption extends HttpMethod {
	private static final long serialVersionUID = 1L;

	public static final String TYPE_ID = "ProtocolTomcat";

	public TomcatMonitorMethodOption() {
		super();
	}

	public TomcatMonitorMethodOption(MonitorMethod copy) {
		super(copy);
	}

	@Override
	public int getPort() {
		return getProperties().getByMethod(8080);
	}	

}