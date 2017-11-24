package com.broada.carrier.monitor.server.impl.entity;

import java.util.Map;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ServerSideMonitorMethod extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	public static final String CLASS_CODE = "Protocol";
	private String id;

	public ServerSideMonitorMethod() {
		super();
	}

	public ServerSideMonitorMethod(MonitorMethod copy) {
		super(copy);
		if (copy instanceof ServerSideMonitorMethod)
			setId(((ServerSideMonitorMethod) copy).getId());
	}

	public ServerSideMonitorMethod(String id, String code, String name, String typeId, String descr, Map<String, Object> options, long modified, String extra) {
		super(code, name, typeId, descr, options, modified, extra);
		setId(id);
	}

	@JsonIgnore
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
