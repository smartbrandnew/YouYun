package com.broada.carrier.monitor.impl.common.entity;

public enum HealthState {
	NORMAL(1, "正常"), ERROR(0, "异常");

	private int id;
	private String descr;

	private HealthState(int id, String descr) {
		this.id = id;
		this.descr = descr;
	}

	public int getId() {
		return id;
	}

	public String getDescr() {
		return descr;
	}

	@Override
	public String toString() {
		return getDescr();
	}
}
