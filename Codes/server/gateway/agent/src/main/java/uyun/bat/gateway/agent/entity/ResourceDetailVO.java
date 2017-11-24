package uyun.bat.gateway.agent.entity;

import uyun.bat.gateway.agent.entity.newentity.Device1;

import java.util.List;

public class ResourceDetailVO {
	private Device1 dev;
	private List<ResourceInfo> info;

	public Device1 getDev() {
		return dev;
	}

	public void setDev(Device1 dev) {
		this.dev = dev;
	}

	public List<ResourceInfo> getInfo() {
		return info;
	}

	public void setInfo(List<ResourceInfo> info) {
		this.info = info;
	}

	public ResourceDetailVO(Device1 dev) {
		super();
		this.dev = dev;
	}

	public ResourceDetailVO() {
		super();
	}

	public ResourceDetailVO(Device1 dev, List<ResourceInfo> info) {
		super();
		this.dev = dev;
		this.info = info;
	}

}
