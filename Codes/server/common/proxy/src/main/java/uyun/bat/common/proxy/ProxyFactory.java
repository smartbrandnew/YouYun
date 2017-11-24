package uyun.bat.common.proxy;

import java.util.HashMap;
import java.util.Map;

public abstract class ProxyFactory {
	private static Map<Object, Object> proxyMap = new HashMap<Object, Object>();

	/**
	 * 获取对应的proxy
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createProxy(Class<T> clazz) {
		return (T) proxyMap.get(clazz);
	}

	/**
	 * 由各个{@link Factory}往这里塞proxy
	 * @param clazz
	 * @param proxy
	 */
	public static <T> void registerProxy(Class<T> clazz, Object proxy) {
		proxyMap.put(clazz, proxy);
	}

}
