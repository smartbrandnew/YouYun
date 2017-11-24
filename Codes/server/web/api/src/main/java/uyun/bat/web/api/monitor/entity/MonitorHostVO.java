package uyun.bat.web.api.monitor.entity;

import java.util.List;

public class MonitorHostVO {
	private String host;
	private List<MonitorHostStateVO> hostStates;

	public MonitorHostVO() {
		super();
	}

	public MonitorHostVO(String host, List<MonitorHostStateVO> hostStates) {
		super();
		this.host = host;
		this.hostStates = hostStates;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public List<MonitorHostStateVO> getHostStates() {
		return hostStates;
	}

	public void setHostStates(List<MonitorHostStateVO> hostStates) {
		this.hostStates = hostStates;
	}
}
