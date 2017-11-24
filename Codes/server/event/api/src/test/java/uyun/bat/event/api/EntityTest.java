package uyun.bat.event.api;

import org.junit.Test;

import uyun.bat.common.test.entity.EntityTestUtil;

public class EntityTest {
	@Test
	public void test(){
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.event.api");
		} catch (Exception e) {
		}
	}
}
