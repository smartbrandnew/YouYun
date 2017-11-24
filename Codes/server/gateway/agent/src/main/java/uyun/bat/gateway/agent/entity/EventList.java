package uyun.bat.gateway.agent.entity;

import java.util.List;

public class EventList {
	private int totalCount;
	private int pageSize;
	private int current;
	private List<EventVO> lists;

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

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public List<EventVO> getLists() {
		return lists;
	}

	public void setLists(List<EventVO> lists) {
		this.lists = lists;
	}

	public EventList(int pageSize, int current) {
		super();
		this.pageSize = pageSize;
		this.current = current;
	}

	public EventList(int pageSize, int current, List<EventVO> lists) {
		super();
		this.pageSize = pageSize;
		this.current = current;
		this.lists = lists;
	}

	public EventList(int totalCount, int pageSize, int current, List<EventVO> lists) {
		super();
		this.totalCount = totalCount;
		this.pageSize = pageSize;
		this.current = current;
		this.lists = lists;
	}

}
