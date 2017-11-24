package com.broada.carrier.monitor.method.webspheremq;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class WebSphereMQMethod extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolWSMQ";

	public WebSphereMQMethod() {
	}

	public WebSphereMQMethod(MonitorMethod copy) {
		super(copy);
	}

	public int getPort() {
		return getProperties().getByMethod(1414);
	}

	public void setPort(int port) {
		getProperties().setByMethod(port);
	}

	public String getCcsId() {
		return getProperties().getByMethod("1208");
	}

	public void setCcsId(String ccsId) {
		getProperties().setByMethod(ccsId);
	}

}
