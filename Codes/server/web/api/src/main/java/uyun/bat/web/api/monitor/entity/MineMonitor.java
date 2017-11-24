package uyun.bat.web.api.monitor.entity;

import java.util.List;

public class MineMonitor {

	/**
	 * 前端展现monitor格式
	 */
	private int currentPage;
	private int total;
	private List<MonitorVO> monitors;

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<MonitorVO> getMonitors() {
		return monitors;
	}

	public void setMonitors(List<MonitorVO> monitors) {
		this.monitors = monitors;
	}

}
