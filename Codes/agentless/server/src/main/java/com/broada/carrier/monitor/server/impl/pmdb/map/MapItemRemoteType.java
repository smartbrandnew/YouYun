package com.broada.carrier.monitor.server.impl.pmdb.map;

public enum MapItemRemoteType {
	ATTRIBUTE("attr"),
	RELATIONSHIP("rs"),
	STATE("state"),
	PERF_GROUP("perf");

	private String alias;

	private MapItemRemoteType(String alias) {
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	public static MapItemRemoteType checkByAlias(String alias) {
		for (MapItemRemoteType type : values())
			if (type.getAlias().equalsIgnoreCase(alias))
				return type;
		throw new IllegalArgumentException("未知的映射远程类型：" + alias);
	}
}
