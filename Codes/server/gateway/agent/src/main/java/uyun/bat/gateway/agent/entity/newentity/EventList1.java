package uyun.bat.gateway.agent.entity.newentity;

import java.util.List;

public class EventList1 {
	private int total;
	private int page_index;
	private int page_size;
	private List<EventVO1> lists;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total_count) {
		this.total = total_count;
	}

	public int getPage_size() {
		return page_size;
	}

	public void setPage_size(int page_size) {
		this.page_size = page_size;
	}

	public int getPage_index() {
		return page_size;
	}

	public void setPage_index(int page_index) {
		this.page_size = page_index;
	}

	public List<EventVO1> getLists() {
		return lists;
	}

	public void setLists(List<EventVO1> lists) {
		this.lists = lists;
	}

	public EventList1(int page_size, int page_index) {
		super();
		this.page_index = page_index;
		this.page_size = page_size;
	}

	public EventList1(int page_size, int page_index, List<EventVO1> lists) {
		super();
		this.page_size = page_size;
		this.page_index = page_index;
		this.lists = lists;
	}

	public EventList1(int total, int page_size, int page_index, List<EventVO1> lists) {
		super();
		this.total = total;
		this.page_size = page_size;
		this.page_index = page_index;
		this.lists = lists;
	}

}
