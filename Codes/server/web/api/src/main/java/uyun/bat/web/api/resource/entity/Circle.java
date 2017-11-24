package uyun.bat.web.api.resource.entity;

import java.util.List;

public class Circle {
	private String id;
	private String ip;
	private String hostName;
	private int size;
	private List<Apps> apps;
	private List<String> tags;
	private Double indication;
	private boolean state;
	private Integer severity;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public List<Apps> getApps() {
		return apps;
	}

	public void setApps(List<Apps> apps) {
		this.apps = apps;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Double getIndication() {
		return indication;
	}

	public void setIndication(Double indication) {
		this.indication = indication;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public Integer getSeverity() {
		return severity;
	}

	public void setSeverity(Integer severity) {
		this.severity = severity;
	}

	public Circle() {
	}

	public Circle(String id) {
		this.id = id;
	}
}
