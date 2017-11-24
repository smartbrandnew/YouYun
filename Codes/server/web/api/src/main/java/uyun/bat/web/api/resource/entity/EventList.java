package uyun.bat.web.api.resource.entity;

import java.util.List;

/**
 * 给前端提供1天资源关联的事件列表
 */
public class EventList {
	private int total;
	private List<EventVO> lists;
	private int current;
	private int pageSize;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<EventVO> getLists() {
		return lists;
	}

	public void setLists(List<EventVO> lists) {
		this.lists = lists;
	}

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public EventList() {
	}

	public EventList(int total, List<EventVO> lists) {
		this.total = total;
		this.lists = lists;
	}
}
