package uyun.bat.datastore.api.mq;

import org.junit.Test;

import uyun.bat.common.test.entity.EntityTestUtil;

public class MQTest {
	@Test
	public void test(){
		try {
			EntityTestUtil.testPackageClasses("uyun.bat.datastore.api.mq");
		} catch (Exception e) {
		}
	}
}
