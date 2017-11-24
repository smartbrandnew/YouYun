package uyun.bat.web.api.resource.entity;

import uyun.bat.datastore.api.entity.ResourceMetrics;

import java.util.List;

public class ResourceMetricsList {
	private int totalCount;
	private int pageSize;
	private int currentPage;
	private List<ResourceMetrics> lists;

	public ResourceMetricsList(int totalCount, int pageSize, int currentPage) {
		this.totalCount = totalCount;
		this.pageSize = pageSize;
		this.currentPage = currentPage;
	}

	public ResourceMetricsList() {
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
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

	public List<ResourceMetrics> getLists() {
		return lists;
	}

	public void setLists(List<ResourceMetrics> lists) {
		this.lists = lists;
	}
}
