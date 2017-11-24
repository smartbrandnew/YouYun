package uyun.bat.common.test.entity;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EntityTest {
	@Test
	public void test() {
		try {
			Object test = EntityTestUtil.create(Entity.class);
			assertTrue(test instanceof Entity);
		} catch (Exception e) {
			System.out.println("实例化对象失败");
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
