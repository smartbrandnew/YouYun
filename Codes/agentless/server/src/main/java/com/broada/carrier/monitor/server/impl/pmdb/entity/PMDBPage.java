package com.broada.carrier.monitor.server.impl.pmdb.entity;

import com.broada.carrier.monitor.common.entity.DefaultDynamicObject;

public class PMDBPage {
	private int totalCount;
	private int recordCount;
	private int page;
	private int pageSize;
	private DefaultDynamicObject[] data;

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public DefaultDynamicObject[] getData() {
		return data;
	}

	public void setData(DefaultDynamicObject[] data) {
		this.data = data;
	}

}
