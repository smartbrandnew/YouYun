package com.broada.carrier.monitor.impl.common.entity;

import java.nio.charset.Charset;

public enum RunState {
	RUNNING(1, "运行"), STOP(0, "停止");

	private int id;
	private String descr;

	private RunState(int id, String descr) {
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
