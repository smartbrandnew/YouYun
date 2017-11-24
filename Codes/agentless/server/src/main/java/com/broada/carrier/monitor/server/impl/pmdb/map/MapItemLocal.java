package com.broada.carrier.monitor.server.impl.pmdb.map;

import com.broada.component.utils.error.ErrorUtil;

public class MapItemLocal {
	private MapObjectType type;
	private String code;

	public MapItemLocal(String local) {
		try {
			int pos = local.indexOf('.');
			if (pos < 0) {
				type = MapObjectType.PERF;
				code = local;
			} else {			
				type = MapObjectType.valueOf(local.substring(0, pos).toUpperCase());
				code = local.substring(pos + 1);
			}			
		} catch (Throwable e) {
			throw ErrorUtil.createIllegalArgumentException("错误的本地属性编码：" + local, e);			
		}
	}

	public MapObjectType getType() {
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
