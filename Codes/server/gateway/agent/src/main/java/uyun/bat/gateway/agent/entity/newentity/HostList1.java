package uyun.bat.gateway.agent.entity.newentity;

import java.util.List;

import uyun.bat.gateway.agent.entity.HostVO;

public class HostList1 {
	private int total;
	private int page_size;
	private int page_index;
	private List<HostVO> lists;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getPage_size() {
		return page_size;
	}

	public void setPage_size(int page_size) {
		this.page_size = page_size;
	}

	public int getPage_index() {
		return page_index;
	}

	public void setPage_index(int page_index) {
		this.page_index = page_index;
	}

	public List<HostVO> getLists() {
		return lists;
	}

	public void setLists(List<HostVO> lists) {
		this.lists = lists;
	}

	public HostList1(int page_size, int page_index, List<HostVO> lists) {
		super();
		this.page_size = page_size;
		this.page_index = page_index;
		this.lists = lists;
	}

	public HostList1(int total, int page_size, int page_index, List<HostVO> lists) {
		super();
		this.total = total;
		this.page_size = page_size;
		this.page_index = page_index;
		this.lists = lists;
	}

}
