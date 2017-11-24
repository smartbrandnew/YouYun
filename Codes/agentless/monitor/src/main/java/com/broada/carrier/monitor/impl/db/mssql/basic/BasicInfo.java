package com.broada.carrier.monitor.impl.db.mssql.basic;

import java.io.Serializable;

public class BasicInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private Object value;
	private int sort = 0;
	
	public BasicInfo() {
		super();
	}

	public BasicInfo(String name, Object value, int sort) {
		this.name = name;
		this.value = value;
		this.sort = sort;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}
	
	public int getSort() {
		return sort;
	}
}
