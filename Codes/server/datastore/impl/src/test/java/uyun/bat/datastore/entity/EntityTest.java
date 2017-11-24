package uyun.bat.datastore.entity;

import org.junit.Test;

import uyun.bat.common.test.entity.EntityTestUtil;

public class EntityTest {
	@Test
	public void test(){
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.datastore.entity");
		} catch (Exception e) {
		}
	}
	
}
