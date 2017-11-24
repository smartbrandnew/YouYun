package com.broada.carrier.monitor.method.script;

/**
 * 脚本执行环境
 * @author Jiangjw
 */
public enum ScriptExecuteSide {
	/**
	 * 脚本会从server推送到probe所在机器执行
	 */
	PROBE("Server Side", "Probe端", "脚本会从server推送到probe所在机器执行"),
	/**
	 * 脚本会从server推送到agent所在机器执行
	 */
	AGENT("Agent Side", "Agent端", "脚本会从server推送到probe所在机器执行");

	private String id;
	private String name;
	private String descr;

	private ScriptExecuteSide(String id, String name, String descr) {
		this.id = id;
		this.name = name;
		this.descr = descr;
	}

	public String getDescr() {
		return descr;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}	

	@Override
	public String toString() {
		return name;
	}

	/**
	 * 根据ID获取脚本执行环境
	 * @param id
	 * @return
	 */
	public static ScriptExecuteSide checkById(String id) {
		for (ScriptExecuteSide item : values())
			if (item.getId().equalsIgnoreCase(id))
				return item;
		throw new IllegalArgumentException("未知的脚本执行端：" + id);
	}
}
