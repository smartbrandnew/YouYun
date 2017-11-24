package uyun.bat.gateway.api.service.util;

public class Assert {
	static public void assertEquals(long expected, long actual) {
		if (expected != actual)
			throw new AssertionError();
	}
}
