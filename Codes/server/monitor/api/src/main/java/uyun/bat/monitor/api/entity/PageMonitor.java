package uyun.bat.monitor.api.entity;

import java.util.List;

/**
 * 包含监测器列表和总数返回
 * 
 */
public class PageMonitor {
	private int count;
	private List<Monitor> monitors;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<Monitor> getMonitors() {
		return monitors;
	}

	public void setMonitors(List<Monitor> monitors) {
		this.monitors = monitors;
	}

}
