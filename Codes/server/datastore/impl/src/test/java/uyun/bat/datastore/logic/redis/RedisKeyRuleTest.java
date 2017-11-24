package uyun.bat.datastore.logic.redis;

import org.junit.Test;

public class RedisKeyRuleTest {
	private static final String key="01234567890";
	@Test
	public void testEncodeResKey() {
		RedisKeyRule.encodeResKey(key);
	}


	@Test
	public void testEncodeMetricKey() {
		RedisKeyRule.encodeMetricKey(key);
	}


	@Test
	public void testEncodeMetricResKey() {
		RedisKeyRule.encodeMetricResKey(key);
	}


}
