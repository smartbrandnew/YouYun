package uyun.bat.event.api.entity;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

public class PageResEvent {
	// 每页条数
	private int pageSize = 10;
	// 当前第几页
	private int currentPage = 1;
	// 查询集合
	private List<ResEvent> lists;
	// 总记录数
	private int total;

	public PageResEvent() {
	}

	public PageResEvent(PageResEvent page) {
		this.pageSize = page.pageSize;
		this.lists = page.lists;
		this.currentPage = page.currentPage;
		this.total = page.total;
	}

	public List<ResEvent> getLists() {
		return lists;
	}

	public void setLists(List<ResEvent> lists) {
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

	@JsonIgnore
	public int getFirstResult() {
		return ((currentPage - 1) * pageSize);
	}
}
