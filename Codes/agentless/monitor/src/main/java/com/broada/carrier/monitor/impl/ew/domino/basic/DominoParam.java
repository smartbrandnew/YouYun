package com.broada.carrier.monitor.impl.ew.domino.basic;

import java.io.Serializable;

public class DominoParam implements Serializable, Cloneable {
	private static final long serialVersionUID = -2044948238545002318L;
	private String dbName;

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
}