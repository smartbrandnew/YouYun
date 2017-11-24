package com.broada.carrier.monitor.impl.db.postgresql.tablespace;

public class PostgreSQLTableSpace {
	public static String[] alermItem = new String[] {"_tsExist", "_Used"};
	private String tsName;
	private double tsSize;
	private String MaxSize;
	private boolean isWacthed;
	
	public boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(boolean isWacthed) {
		this.isWacthed = isWacthed;
	}
	
	public String getTsName() {
		return tsName;
	}

	public void setTsName(String tsName) {
		this.tsName = tsName;
	}

	public double getTsSize() {
		return tsSize;
	}

	public void setTsSize(double tsSize) {
		this.tsSize = tsSize;
	}

	public String getMaxSize() {
		return MaxSize;
	}

	public void setMaxSize(String maxSize) {
		MaxSize = maxSize;
	}
}
