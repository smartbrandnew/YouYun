package com.broada.carrier.monitor.client.impl.impexp.entity;

public enum DataSource {
	BCC, EXCEL, COS;

	public static DataSource check(String name) {
		for (DataSource item : values())
			if (item.name().equalsIgnoreCase(name))
				return item;
		throw new IllegalArgumentException(name);
	}	
}
