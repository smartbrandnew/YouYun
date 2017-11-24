package uyun.bat.web.api.resource.entity;

import java.util.List;

/**
 * 返回资源关联的监测器信息
 */
public class PageResMonitorInfo {
	// 每页条数
	private int pageSize;
	// 当前第几页
	private int currentPage;
	// 查询集合
	private List<ResMonitorInfo> lists;
	// 总记录数
	private int total;

	public PageResMonitorInfo() {
	}

	public List<ResMonitorInfo> getLists() {
		return lists;
	}

	public void setLists(List<ResMonitorInfo> lists) {
		this.lists = lists;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

}
