package uyun.bat.web.api.monitor.entity;

import java.util.List;

public class MineNotifyRecord {

	private int currentPage;
	private int total;
	private List<NotifyRecordVO> lists;

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

	public List<NotifyRecordVO> getLists() {
		return lists;
	}

	public void setLists(List<NotifyRecordVO> lists) {
		this.lists = lists;
	}

}
