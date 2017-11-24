package uyun.bat.common.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestConfig {
	@Test
	public void test() {
		Config.getInstance().dumpConfigItem();
		assertEquals("jdbc:mysql://" + Config.getInstance().get("mysql.ip")
				+ ":" + Config.getInstance().get("mysql.port")
				+ "/bat?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowMultiQueries=true",
				Config.getInstance().get("jdbc.url"));
	}
}
