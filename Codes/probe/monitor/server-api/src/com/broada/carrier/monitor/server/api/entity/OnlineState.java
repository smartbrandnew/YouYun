package com.broada.carrier.monitor.server.api.entity;

public enum OnlineState {
	UNKNOWN("未知"), ONLINE("在线"), OFFLINE("离线"), TESTING("检查中");	
	
	private String descr;

	private OnlineState(String descr) {
		this.descr = descr;
	}

	public String getDescr() {
		return descr;
	}

	@Override
	public String toString() {
		return getDescr();
	}
}
