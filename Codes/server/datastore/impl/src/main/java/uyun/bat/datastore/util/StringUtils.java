package uyun.bat.datastore.util;

public class StringUtils {
	public static boolean isNotNullAndTrimBlank(String str) {
		if (str != null && str.trim().length() > 0)
			return true;
		return false;
	}

	public static boolean isNotNullAndBlank(String str) {
		if (str == null || str.length() <= 0)
			return false;
		return true;
	}

	public static boolean isBlank(String str) {
		if (str.trim().length() <= 0)
			return true;
		return false;
	}

	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	public static boolean isNotNull(String str) {
		return str == null ? false : true;
	}

	public static boolean isNotTrimBlank(String str) {
		if (str != null && str.trim().length() > 0)
			return true;
		return false;
	}
}
