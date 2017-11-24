package uyun.bat.gateway.agent.entity;

import java.util.List;

public class HostList {
	private int totalCount;
	private int pageSize;
	private int current;
	private List<HostVO> lists;

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

	public List<HostVO> getLists() {
		return lists;
	}

	public void setLists(List<HostVO> lists) {
		this.lists = lists;
	}

	public HostList(int pageSize, int current, List<HostVO> lists) {
		super();
		this.pageSize = pageSize;
		this.current = current;
		this.lists = lists;
	}

	public HostList(int totalCount, int pageSize, int current, List<HostVO> lists) {
		super();
		this.totalCount = totalCount;
		this.pageSize = pageSize;
		this.current = current;
		this.lists = lists;
	}

}
