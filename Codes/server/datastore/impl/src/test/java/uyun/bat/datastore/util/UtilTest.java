package uyun.bat.datastore.util;

import org.junit.Test;

import uyun.bat.common.test.entity.EntityTestUtil;

public class UtilTest {
	@Test
	public void test(){
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.datastore.util");
		} catch (Exception e) {
		}
	}
	
}
