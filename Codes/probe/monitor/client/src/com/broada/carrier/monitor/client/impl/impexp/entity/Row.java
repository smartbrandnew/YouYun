package com.broada.carrier.monitor.client.impl.impexp.entity;


public class Row {
	private String[] values;
	
	Row(int colCount) {
		values = new String[colCount];
	}
	
	public void setValue(int col, String value) {
		values[col] = value;
	}

	public String getValue(int col) {
		return values[col];
	}

}
