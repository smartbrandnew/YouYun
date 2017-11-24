package uyun.bat.datastore.util;

public class SystemProperties {
	private static SimpleProperties props;

	/**
	 * 获取系统属性整形值
	 * @param property
	 * @param defValue
	 * @return
	 */
	public static int get(String property, int defValue) {
		return getProps().get(property, defValue);
	}

	/**
	 * 获取系统属性浮点值
	 * @param property
	 * @param defValue
	 * @return
	 */
	public static double get(String property, double defValue) {
		return getProps().get(property, defValue);
	}

	/**
	 * 获取系统属性字符串值
	 * @param property
	 * @param defValue
	 * @return
	 */
	public static String get(String property, String defValue) {
		return getProps().get(property, defValue);
	}

	/**
	 * 获取系统属性布尔值
	 * @param property
	 * @param defValue
	 * @return
	 */
	public static boolean get(String property, boolean defValue) {
		return getProps().get(property, defValue);
	}

	/**
	 * 判断系统是否有配置指定属性，如果没有则进行设置
	 * @param property
	 * @param value
	 */
	public static void setIfNotExists(String property, String value) {
		if (System.getProperty(property) == null)
			System.setProperty(property, value);
	}

	public static void setIfNotExists(String property, Object value) {
		if (System.getProperty(property) == null)
			System.setProperty(property, value == null ? null : value.toString());
	}

	private static SimpleProperties getProps() {
		if (props == null) {
			synchronized (SimpleProperties.class) {
				if (props == null)
					props = new SimpleProperties(System.getProperties());
			}
		}
		return props;
	}
}
