package uyun.bat.web.impl.common.entity;

import org.junit.Test;

import uyun.bat.common.test.entity.EntityTestUtil;

public class EntityTest {
	@Test
	public void test(){
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.web.impl.common.entity");
		} catch (Exception e) {
		}
	}
	
}
