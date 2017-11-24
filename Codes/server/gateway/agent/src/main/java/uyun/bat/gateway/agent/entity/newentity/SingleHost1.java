package uyun.bat.gateway.agent.entity.newentity;

import java.util.Date;
import java.util.List;

import uyun.bat.gateway.agent.entity.HostVO;
import uyun.bat.gateway.agent.entity.ResourceInfo;

public class SingleHost1 extends HostVO {
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

	public SingleHost1() {
		super();
	}

	public SingleHost1(String id, String name, String ip, String type, Date modified, List<String> tags,
					   List<String> apps, Device1 dev, List<ResourceInfo> info, String os) {
		super(id, name, ip, type, modified, tags, apps,os, true);
		this.dev = dev;
		this.info = info;
	}

}
