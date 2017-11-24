package uyun.bat.web.api.resource.entity;

import java.util.List;

public class ResourceList1 {
	private int totalCount;
	private int pageSize;
	private int currentPage;
	private List<ResourceVO1> lists;
	private List<String> defaultExpandedRowKeys;

	// 在线资源统计数
	private int onlineCount;
	// 离线资源统计数
	private int offlineCount;

	public int getOnlineCount() {
		return onlineCount;
	}

	public void setOnlineCount(int onlineCount) {
		this.onlineCount = onlineCount;
	}

	public int getOfflineCount() {
		return offlineCount;
	}

	public void setOfflineCount(int offlineCount) {
		this.offlineCount = offlineCount;
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

	public List<ResourceVO1> getLists() {
		return lists;
	}

	public void setLists(List<ResourceVO1> lists) {
		this.lists = lists;
	}

	public List<String> getDefaultExpandedRowKeys() {
		return defaultExpandedRowKeys;
	}

	public void setDefaultExpandedRowKeys(List<String> defaultExpandedRowKeys) {
		this.defaultExpandedRowKeys = defaultExpandedRowKeys;
	}

}
