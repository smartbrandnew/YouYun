package com.broada.carrier.monitor.impl.db.oracle.tablestate;

import java.io.Serializable;

public class CollectRequest implements Serializable {
	public static final int ACTION_GET_TABLES = 0;
	public static final int ACTION_GET_TABLE_DETAIL = 1;
	private static final long serialVersionUID = 1L;
	private int action;
	private int tableNum;
	private int pageIndex;
	private String tableFilter;
	private String tableName;
	
	public CollectRequest() {
	}

	public CollectRequest(int action, int tableNum, int pageIndex, String tableFilter, String tableName) {
		this.action = action;
		this.tableNum = tableNum;
		this.pageIndex = pageIndex;
		this.tableFilter = tableFilter;
		this.tableName = tableName;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public int getTableNum() {
		return tableNum;
	}

	public void setTableNum(int tableNum) {
		this.tableNum = tableNum;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public String getTableFilter() {
		return tableFilter;
	}

	public void setTableFilter(String tableFilter) {
		this.tableFilter = tableFilter;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public static CollectRequest createTableDetailRequest(String tablename) {
		return new CollectRequest(ACTION_GET_TABLE_DETAIL, 0, 0, null, tablename);		
	}
	
	public static CollectRequest createTablesRequest(int tableNum, int pageIndex, String tableFilter) {
		return new CollectRequest(ACTION_GET_TABLES, tableNum, pageIndex, tableFilter, null);		
	}

}
