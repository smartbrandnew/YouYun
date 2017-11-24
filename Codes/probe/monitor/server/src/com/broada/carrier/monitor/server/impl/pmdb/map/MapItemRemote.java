package com.broada.carrier.monitor.server.impl.pmdb.map;

public class MapItemRemote {
	private MapItemRemoteType type;
	private String code;

	public MapItemRemote(String remote) {
		int pos = remote.indexOf('.');
		if (pos < 0) {
			type = MapItemRemoteType.ATTRIBUTE;
			code = remote;
		} else {
			type = MapItemRemoteType.checkByAlias(remote.substring(0, pos));
			code = remote.substring(pos + 1);
		}
	}

	public MapItemRemoteType getType() {
		return type;
	}

	public String getCode() {
		return code;
	}

	@Override
	public String toString() {
		return String.format("%s[%s.%s]", getClass().getSimpleName(), getType(), getCode());
	}
}
