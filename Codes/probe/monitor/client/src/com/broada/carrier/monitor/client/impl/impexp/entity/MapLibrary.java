package com.broada.carrier.monitor.client.impl.impexp.entity;

import com.broada.carrier.monitor.common.util.TextUtil;
import com.broada.component.utils.lang.SimpleProperties;

public class MapLibrary extends SimpleProperties {
	private static MapLibrary instance;

	public MapLibrary(String filename) {
		super(filename);
	}

	/**
	 * 获取默认实例
	 * @return
	 */
	public static MapLibrary getDefault() {
		if (instance == null) {
			synchronized (MapLibrary.class) {
				if (instance == null)
					instance = new MapLibrary(System.getProperty("user.dir") + "/conf/impexp-map.properties");
			}
		}
		return instance;
	}

	@Override
	public String check(String name) {		
		String result = super.check(name);
		if (TextUtil.isEmpty(result))
			throw new IllegalArgumentException("未知的映射：" + name);
		return result;
	}	
}
