package uyun.bat.datastore.api.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class EncryptUtilTest {

	@Test
	public void testHash() {
		EncryptUtil.hash("test".getBytes());
	}

	@Test
	public void testString2MD5() {
		EncryptUtil.string2MD5("test");
	}

	@Test
	public void testConvertMD5() {
		EncryptUtil.convertMD5("test");
	}

	@Test
	public void testMain() {
		EncryptUtil.main(new String[]{});
	}

}
