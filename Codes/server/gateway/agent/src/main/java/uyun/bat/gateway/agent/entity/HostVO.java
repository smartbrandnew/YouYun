package uyun.bat.gateway.agent.entity;

import java.util.Date;
import java.util.List;

public class HostVO {
	private String id;
	private String name;
	private String ip;
	private String type = "Server";
	private Date modified;
	private List<String> tags;
	private List<String> apps;
	private String os;
	private boolean online_state = true;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getApps() {
		return apps;
	}

	public void setApps(List<String> apps) {
		this.apps = apps;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public HostVO() {
		super();
	}

	public HostVO(String id, String name, String ip, String type, Date modified, List<String> tags,
			List<String> apps, String os, boolean online_state) {
		super();
		this.id = id;
		this.name = name;
		this.ip = ip;
		this.type = type;
		this.modified = modified;
		this.tags = tags;
		this.apps = apps;
        this.os = os;
        this.setOnline_state(online_state);
	}

	public boolean isOnline_state() {
		return online_state;
	}

	public void setOnline_state(boolean online_state) {
		this.online_state = online_state;
	}

}
