package com.broada.carrier.monitor.server.api.entity;

public enum MonitorState {
	/**
	 * 未监测
	 */
	UNMONITOR("未监测", "未监测"),
	/**
	 * 监测成功
	 */
	SUCCESSED("正常", "监测成功"),
	/**
	 * 监测失败
	 */
	FAILED("异常", "监测失败"),
	OVERSTEP("异常", "监测失败");

	private String descr;
	private String displayName;

	private MonitorState(String displayName, String descr) {
		this.displayName = displayName;
		this.descr = descr;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescr() {
		return descr;
	}

	public int getId() {
		return ordinal();
	}

	public boolean isError() {
		return this.equals(FAILED) || this.equals(OVERSTEP);
	}

	public static MonitorState checkById(int id) {
		for (MonitorState state : values())
			if (state.getId() == id)
				return state;
		throw new IllegalArgumentException("未知的id：" + id);
	}

	public static MonitorState checkByName(String name) {
		for (MonitorState state : values())
			if (state.name().equalsIgnoreCase(name))
				return state;
		throw new IllegalArgumentException("未知的name：" + name);
	}
}
