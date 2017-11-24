package uyun.bat.gateway.agent.entity;

import java.util.Date;
import java.util.List;

public class SingleHost extends HostVO {
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

	public SingleHost() {
		super();
	}

	public SingleHost(String id, String name, String ip, String type, Date modified, List<String> tags,
			List<String> apps, Device dev, List<ResourceInfo> info, String os) {
		super(id, name, ip, type, modified, tags, apps,os, true);
		this.dev = dev;
		this.info = info;
	}

	public SingleHost(Device dev, List<ResourceInfo> info) {
		this.dev = dev;
		this.info = info;
	}
}
