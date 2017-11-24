package uyun.bat.datastore.util;



import org.junit.Assert;
import org.junit.Test;

public class SystemPropertiesTest {

	@Test
	public void testGetStringString() {
		Assert.assertNotNull( SystemProperties.get("user.dir", "aaa"));
	}

	@Test
	public void testSetIfNotExistsStringString() {
		SystemProperties.setIfNotExists("aaa", "bbb");
		Assert.assertEquals("bbb", SystemProperties.get("aaa", "aaa"));;
	}

}
