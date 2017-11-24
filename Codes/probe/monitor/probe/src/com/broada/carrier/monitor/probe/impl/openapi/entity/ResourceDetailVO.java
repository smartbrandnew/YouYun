package com.broada.carrier.monitor.probe.impl.openapi.entity;

import java.util.List;

public class ResourceDetailVO {
	private Device dev;
	private List<ResourceInfo> info;

	public Device getDev() {
		return dev;
	}

	public void setDev(Device dev) {
		this.dev = dev;
	}

	public List<ResourceInfo> getInfo() {
		return info;
	}

	public void setInfo(List<ResourceInfo> info) {
		this.info = info;
	}

	public ResourceDetailVO(Device dev) {
		super();
		this.dev = dev;
	}

	public ResourceDetailVO() {
		super();
	}

	public ResourceDetailVO(Device dev, List<ResourceInfo> info) {
		super();
		this.dev = dev;
		this.info = info;
	}

}
