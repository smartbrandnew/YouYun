package uyun.bat.datastore.entity;

import java.util.List;

public class ListWrapper {
	private List<?> list;

	public ListWrapper(List<?> list) {
		this.list = list;
	}

	public List<?> getList() {
		return list;
	}

	public int getSize() {
		return list.size();
	}
}
