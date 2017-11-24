package com.broada.carrier.monitor.server.api.entity;

public enum CollectMonitorState {
	/**
	 * 
	 * 监测开始
	 */
	START("监测开始", "监测开始"),
	/**
	 * 监测成功
	 */
	SUCCESSED("正常", "监测成功"),
	/**
	 * 监测失败
	 */
	FAILED("异常", "监测失败"),
	/**
	 * 处理中
	 */
	PROCESSING("处理中", "处理中");

	private String descr;
	private String displayName;

	private CollectMonitorState(String displayName, String descr) {
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
		return this.equals(FAILED);
	}

	public static CollectMonitorState checkById(int id) {
		for (CollectMonitorState state : values())
			if (state.getId() == id)
				return state;
		throw new IllegalArgumentException("未知的id：" + id);
	}

	public static CollectMonitorState checkByName(String name) {
		for (CollectMonitorState state : values())
			if (state.name().equalsIgnoreCase(name))
				return state;
		throw new IllegalArgumentException("未知的name：" + name);
	}
}
