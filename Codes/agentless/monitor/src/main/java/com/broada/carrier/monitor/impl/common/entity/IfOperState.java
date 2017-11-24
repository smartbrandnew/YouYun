package com.broada.carrier.monitor.impl.common.entity;

public enum IfOperState {
	UP(1, "工作"), DOWN(2, "停止"), UNKNOWN(3, "未知");

	int id;
	String descr;

	private IfOperState(int id, String descr) {
		this.id = id;
		this.descr = descr;
	}

	@Override
	public String toString() {
		return getDescr();
	}

	public int getId() {
		return id;
	}

	public String getDescr() {
		return descr;
	}

	public static IfOperState checkById(int id) {
		for (IfOperState state : values()) {
			if (state.getId() == id)
				return state;
		}
		return IfOperState.UNKNOWN;
	}
}
