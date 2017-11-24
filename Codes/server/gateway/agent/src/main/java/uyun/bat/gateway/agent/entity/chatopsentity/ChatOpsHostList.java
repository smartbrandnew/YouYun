package uyun.bat.gateway.agent.entity.chatopsentity;

import java.util.List;

public class ChatOpsHostList {
	private int total;
	private int page_size;
	private int page_index;
	private List<ChatOpsHost> lists;

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

	public List<ChatOpsHost> getLists() {
		return lists;
	}

	public void setLists(List<ChatOpsHost> lists) {
		this.lists = lists;
	}

	public ChatOpsHostList(int page_size, int page_index, List<ChatOpsHost> lists) {
		super();
		this.page_size = page_size;
		this.page_index = page_index;
		this.lists = lists;
	}

	public ChatOpsHostList(int total, int page_size, int page_index, List<ChatOpsHost> lists) {
		super();
		this.total = total;
		this.page_size = page_size;
		this.page_index = page_index;
		this.lists = lists;
	}

}
