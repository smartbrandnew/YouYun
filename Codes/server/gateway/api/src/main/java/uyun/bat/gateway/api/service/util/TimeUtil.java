package uyun.bat.gateway.api.service.util;

public abstract class TimeUtil {
	/**
	 * 超过服务器一天时间的
	 * @return
	 */
	public static long getExpireTime() {
		return System.currentTimeMillis() + 24 * 60 * 60 * 1000;
	}
}
