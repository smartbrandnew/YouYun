package com.broada.carrier.monitor.impl.common;

public class StringUtil {
	public static String convertNull2Blank(Object value) {
		if (value == null)
			return "";
		return value.toString();
	}
}
